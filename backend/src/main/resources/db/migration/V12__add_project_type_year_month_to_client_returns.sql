-- Migración V12: Añadir project_type, year y month a client_returns

ALTER TABLE client_returns ADD COLUMN project_type VARCHAR(30) NOT NULL DEFAULT 'FABRICA';
ALTER TABLE client_returns ADD COLUMN `year` INT NOT NULL DEFAULT 2026;
ALTER TABLE client_returns ADD COLUMN `month` INT NOT NULL DEFAULT 1;
