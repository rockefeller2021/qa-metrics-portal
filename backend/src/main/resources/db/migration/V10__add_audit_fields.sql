-- Migración V10: Columnas de auditoría para trazabilidad de usuario (created_by, last_modified_by, updated_at)

ALTER TABLE test_executions
    ADD COLUMN created_by VARCHAR(100) NULL,
    ADD COLUMN last_modified_by VARCHAR(100) NULL,
    ADD COLUMN updated_at DATETIME NULL;

ALTER TABLE bugs
    ADD COLUMN created_by VARCHAR(100) NULL,
    ADD COLUMN last_modified_by VARCHAR(100) NULL,
    ADD COLUMN updated_at DATETIME NULL;

ALTER TABLE delivery_slas
    ADD COLUMN created_by VARCHAR(100) NULL,
    ADD COLUMN last_modified_by VARCHAR(100) NULL,
    ADD COLUMN updated_at DATETIME NULL;
