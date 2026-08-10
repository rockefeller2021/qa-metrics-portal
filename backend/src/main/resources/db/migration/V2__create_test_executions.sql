-- ============================================================
-- V2: Tabla principal de Ejecuciones de Prueba
-- QA Metrics Portal — Flyway Migration (MySQL 8)
-- ============================================================
CREATE TABLE test_executions (
    id                   BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    jira_id              VARCHAR(50)   NOT NULL,
    project_type         VARCHAR(20)   NOT NULL,
    assignment_date      DATE          NOT NULL,
    design_date          DATE,
    designer_analyst     VARCHAR(100)  NOT NULL,
    commitment_date      DATE,
    qa_delivery_date     DATE,
    client_delivery_date DATE,
    sprint_or_pi         VARCHAR(50),
    description          TEXT,
    created_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_project_type CHECK (project_type IN ('FABRICA', 'MINOR_DEMAND'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_test_exec_project_type ON test_executions(project_type);
CREATE INDEX idx_test_exec_jira_id      ON test_executions(jira_id);
