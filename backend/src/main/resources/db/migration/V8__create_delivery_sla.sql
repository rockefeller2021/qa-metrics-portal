-- ============================================================
-- V8: Tabla de Módulo de Seguimiento de Entregas y Gobierno SLA (RF04)
-- QA Metrics Portal — Flyway Migration (MySQL 8)
-- ============================================================
CREATE TABLE delivery_slas (
    id                        BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    jira_id                   VARCHAR(50)   NOT NULL UNIQUE,
    project_type              VARCHAR(20)   NOT NULL,
    sprint_or_pi              VARCHAR(50)   NOT NULL,
    designer_analyst          VARCHAR(100)  NOT NULL,
    estimated_delivery_date   DATE          NOT NULL,
    estimated_qa_date         DATE,
    real_qa_date              DATE,
    real_client_delivery_date DATE,
    status                    VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    delay_days                INT           NOT NULL DEFAULT 0,
    notes                     TEXT,
    created_at                DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_delivery_project_type
        CHECK (project_type IN ('FABRICA', 'MINOR_DEMAND')),
    CONSTRAINT chk_delivery_status
        CHECK (status IN ('PENDING', 'ON_TIME', 'DELAYED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_delivery_project_type ON delivery_slas(project_type);
CREATE INDEX idx_delivery_sprint_or_pi ON delivery_slas(sprint_or_pi);
CREATE INDEX idx_delivery_status       ON delivery_slas(status);
