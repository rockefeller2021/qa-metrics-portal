-- ============================================================
-- V6: Agregar conteo de casos (Totales, Exitosos, Fallidos, Bloqueados)
-- QA Metrics Portal — Flyway Migration
-- ============================================================
ALTER TABLE test_executions
    ADD COLUMN total_cases      INT NOT NULL DEFAULT 0,
    ADD COLUMN successful_cases INT NOT NULL DEFAULT 0,
    ADD COLUMN failed_cases     INT NOT NULL DEFAULT 0,
    ADD COLUMN blocked_cases    INT NOT NULL DEFAULT 0;

ALTER TABLE test_execution_runs
    ADD COLUMN cases_executed INT NOT NULL DEFAULT 0,
    ADD COLUMN cases_passed   INT NOT NULL DEFAULT 0,
    ADD COLUMN cases_failed   INT NOT NULL DEFAULT 0,
    ADD COLUMN cases_blocked  INT NOT NULL DEFAULT 0;
