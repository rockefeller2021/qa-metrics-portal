-- Migration V9: Añadir campo developer_name a la tabla bugs
ALTER TABLE bugs ADD COLUMN developer_name VARCHAR(100) NULL AFTER reported_by;
