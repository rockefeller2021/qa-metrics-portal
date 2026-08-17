-- ============================================================
-- V13: Agregar columna request_type a ejecuciones, bugs y SLAs
-- QA Metrics Portal — Flyway Migration (MySQL 8 / H2)
-- ============================================================

ALTER TABLE test_executions ADD COLUMN request_type VARCHAR(30) NOT NULL DEFAULT 'EVOLUTIVO';
ALTER TABLE bugs ADD COLUMN request_type VARCHAR(30) NOT NULL DEFAULT 'EVOLUTIVO';
ALTER TABLE delivery_slas ADD COLUMN request_type VARCHAR(30) NOT NULL DEFAULT 'EVOLUTIVO';

CREATE INDEX idx_test_exec_req_type ON test_executions(request_type);
CREATE INDEX idx_bugs_req_type      ON bugs(request_type);
CREATE INDEX idx_sla_req_type       ON delivery_slas(request_type);
