-- ============================================================
-- V1: Tabla de Usuarios y Seed de Admin
-- QA Metrics Portal — Flyway Migration (MySQL 8)
-- ============================================================
CREATE TABLE users (
    id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)   NOT NULL UNIQUE,
    email         VARCHAR(100)  NOT NULL UNIQUE,
    password_hash VARCHAR(255)  NOT NULL,
    role          VARCHAR(20)   NOT NULL DEFAULT 'ANALYST',
    active        TINYINT(1)    NOT NULL DEFAULT 1,
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_role CHECK (role IN ('ADMIN', 'ANALYST'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed: usuario administrador inicial
-- Password: Admin1234! (hash BCrypt strength=12)
INSERT INTO users (username, email, password_hash, role, active)
VALUES (
    'admin_qa',
    'admin@qaportal.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewXEBDHUbLHASOme',
    'ADMIN',
    1
);
