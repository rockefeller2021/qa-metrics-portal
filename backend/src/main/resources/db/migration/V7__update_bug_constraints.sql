-- ============================================================
-- V7: Actualización de Constraint defect_type en Tabla bugs
-- QA Metrics Portal — Flyway Migration (MySQL 8)
-- ============================================================
ALTER TABLE bugs DROP CHECK chk_defect_type;

ALTER TABLE bugs ADD CONSTRAINT chk_defect_type
    CHECK (defect_type IN ('FUNCTIONAL', 'UI_UX', 'PERFORMANCE', 'SECURITY', 'DATA', 'DEVELOPMENT_ERROR', 'REINJECTION', 'REGRESSION', 'ENVIRONMENT', 'REQUIREMENTS_GAP', 'OTHER'));
