-- ============================================================
-- V4: Tabla de BugTracker
-- QA Metrics Portal — Flyway Migration (MySQL 8)
-- ============================================================
CREATE TABLE bugs (
    id               BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    bug_jira_id      VARCHAR(50)   NOT NULL UNIQUE,
    requirement_id   VARCHAR(50)   NOT NULL,
    project_type     VARCHAR(20)   NOT NULL,
    sprint_or_pi     VARCHAR(50)   NOT NULL,
    status           VARCHAR(20)   NOT NULL,
    defect_type      VARCHAR(30)   NOT NULL,
    description      TEXT          NOT NULL,
    reinjection_flag TINYINT(1)    NOT NULL DEFAULT 0,
    reported_date    DATE          NOT NULL,
    resolved_date    DATE,
    reported_by      VARCHAR(100),
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_bug_project_type
        CHECK (project_type IN ('FABRICA', 'MINOR_DEMAND')),
    CONSTRAINT chk_bug_status
        CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REJECTED')),
    CONSTRAINT chk_defect_type
        CHECK (defect_type IN ('DEVELOPMENT_ERROR', 'REINJECTION', 'REGRESSION', 'ENVIRONMENT', 'REQUIREMENTS_GAP', 'OTHER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_bugs_project_type     ON bugs(project_type);
CREATE INDEX idx_bugs_sprint_or_pi     ON bugs(sprint_or_pi);
CREATE INDEX idx_bugs_reinjection_flag ON bugs(reinjection_flag);
CREATE INDEX idx_bugs_requirement_id   ON bugs(requirement_id);
