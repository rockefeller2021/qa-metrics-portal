"""
ETL Script — Migración de Excel a QA Metrics Portal
Despivota columnas de Retest N y pobla PostgreSQL.
Requiere: pip install pandas openpyxl sqlalchemy psycopg2-binary
"""

import pandas as pd
from sqlalchemy import create_engine, text
from datetime import datetime
import os

# ── Configuración ──────────────────────────────────────────────
EXCEL_PATH  = os.getenv("EXCEL_PATH",  "ejecuciones_qa.xlsx")
DB_URL      = os.getenv("DATABASE_URL", "postgresql://postgres:postgres@localhost:5432/qa_metrics_db")
SHEET_NAME  = os.getenv("SHEET_NAME",  "Ejecuciones")

# Columnas base del Excel (ajustar a los nombres reales)
BASE_COLS = {
    "jira_id":           "ID Jira",
    "project_type":      "Tipo Proyecto",   # FABRICA / MINOR_DEMAND
    "assignment_date":   "Fecha Asignación",
    "design_date":       "Fecha Diseño",
    "designer_analyst":  "Analista Diseño",
    "commitment_date":   "Fecha Compromiso",
    "qa_delivery_date":  "Fecha Real QA",
    "client_delivery_date": "Fecha Real Cliente",
    "sprint_or_pi":      "Sprint/PI",
}

# Columnas de retests en el Excel (Retest1_Fecha, Retest1_Analista, Retest1_Estado, etc.)
RETEST_PREFIX  = "Retest"
MAX_RETESTS    = 10

def parse_date(val):
    if pd.isna(val) or val == "":
        return None
    try:
        return pd.to_datetime(val).date()
    except Exception:
        return None


def main():
    print(f"📂 Leyendo Excel: {EXCEL_PATH}")
    df = pd.read_excel(EXCEL_PATH, sheet_name=SHEET_NAME)
    df.columns = df.columns.str.strip()

    engine = create_engine(DB_URL)
    inserted_executions = 0
    inserted_runs       = 0

    with engine.connect() as conn:
        for _, row in df.iterrows():
            # ── Insertar test_execution ────────────────────────
            exec_data = {
                "jira_id":               str(row.get(BASE_COLS["jira_id"], "")).strip(),
                "project_type":          str(row.get(BASE_COLS["project_type"], "FABRICA")).upper().replace(" ", "_"),
                "assignment_date":       parse_date(row.get(BASE_COLS["assignment_date"])),
                "design_date":           parse_date(row.get(BASE_COLS["design_date"])),
                "designer_analyst":      str(row.get(BASE_COLS["designer_analyst"], "N/A")),
                "commitment_date":       parse_date(row.get(BASE_COLS["commitment_date"])),
                "qa_delivery_date":      parse_date(row.get(BASE_COLS["qa_delivery_date"])),
                "client_delivery_date":  parse_date(row.get(BASE_COLS["client_delivery_date"])),
                "sprint_or_pi":          str(row.get(BASE_COLS["sprint_or_pi"], "")),
            }

            if not exec_data["jira_id"]:
                continue

            result = conn.execute(text("""
                INSERT INTO test_executions
                    (jira_id, project_type, assignment_date, design_date, designer_analyst,
                     commitment_date, qa_delivery_date, client_delivery_date, sprint_or_pi)
                VALUES
                    (:jira_id, :project_type, :assignment_date, :design_date, :designer_analyst,
                     :commitment_date, :qa_delivery_date, :client_delivery_date, :sprint_or_pi)
                ON CONFLICT DO NOTHING
                RETURNING id
            """), exec_data)

            exec_id = result.fetchone()
            if not exec_id:
                continue

            exec_id = exec_id[0]
            inserted_executions += 1

            # ── Despivotear columnas de Retest ─────────────────
            for n in range(1, MAX_RETESTS + 1):
                fecha_col    = f"{RETEST_PREFIX}{n}_Fecha"
                analista_col = f"{RETEST_PREFIX}{n}_Analista"
                estado_col   = f"{RETEST_PREFIX}{n}_Estado"

                if fecha_col not in df.columns:
                    break

                fecha    = parse_date(row.get(fecha_col))
                analista = str(row.get(analista_col, "")).strip()
                estado   = str(row.get(estado_col, "")).strip().upper()

                if not fecha or not analista:
                    continue

                # Normalizar estado
                if estado in ("OK", "PASS", "EXITOSO"):   estado = "SUCCESSFUL"
                elif estado in ("FAIL", "FALLIDO"):        estado = "FAILED"
                elif estado in ("BLOQUEADO"):              estado = "BLOCKED"
                else:                                       estado = "RETEST"

                conn.execute(text("""
                    INSERT INTO test_execution_runs
                        (test_execution_id, run_number, execution_date, executed_by_analyst, status)
                    VALUES
                        (:exec_id, :run_number, :execution_date, :analyst, :status)
                    ON CONFLICT DO NOTHING
                """), {
                    "exec_id":        exec_id,
                    "run_number":     n,
                    "execution_date": fecha,
                    "analyst":        analista,
                    "status":         estado,
                })
                inserted_runs += 1

        conn.commit()

    print(f"✅ Migración completada:")
    print(f"   → Ejecuciones insertadas: {inserted_executions}")
    print(f"   → Iteraciones de Retest:  {inserted_runs}")


if __name__ == "__main__":
    main()
