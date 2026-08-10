-- ============================================================
-- V5: Actualizar hash de contraseña admin_qa a BCrypt $2b$12$
-- QA Metrics Portal — Flyway Migration
-- ============================================================
UPDATE users
SET password_hash = '$2b$12$7ryNzp/lyVLs9mPlJwkzxu1CO19q0GojRuoiYJQACOhL84L0VdzqS'
WHERE username = 'admin_qa';
