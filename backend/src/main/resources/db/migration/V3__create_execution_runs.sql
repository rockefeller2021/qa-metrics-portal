-- ============================================================
-- V3: Tabla hija de Iteraciones / Retests (1 a N)
-- QA Metrics Portal — Flyway Migration (MySQL 8)
-- ============================================================
CREATE TABLE test_execution_runs (
    id                  BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    test_execution_id   BIGINT        NOT NULL,
    run_number          INT           NOT NULL,
    execution_date      DATE          NOT NULL,
    executed_by_analyst VARCHAR(100)  NOT NULL,
    status              VARCHAR(20)   NOT NULL,
    notes               TEXT,
    CONSTRAINT fk_execution
        FOREIGN KEY (test_execution_id)
        REFERENCES test_executions(id) ON DELETE CASCADE,
    CONSTRAINT chk_run_status
        CHECK (status IN ('SUCCESSFUL', 'FAILED', 'BLOCKED', 'RETEST')),
    CONSTRAINT uq_run_number
        UNIQUE (test_execution_id, run_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_exec_runs_execution_id ON test_execution_runs(test_execution_id);
CREATE INDEX idx_exec_runs_status       ON test_execution_runs(status);
