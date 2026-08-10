-- Migración V12: Añadir project_type, year y month a client_returns

ALTER TABLE client_returns 
ADD COLUMN project_type VARCHAR(30) NOT NULL DEFAULT 'FABRICA',
ADD COLUMN year INT NOT NULL DEFAULT 2026,
ADD COLUMN month INT NOT NULL DEFAULT 1;
