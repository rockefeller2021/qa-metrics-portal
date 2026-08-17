-- Migración V11: Tablas para Seguimiento de Cliente y Devoluciones IBL

CREATE TABLE client_delivery_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_type VARCHAR(20) NOT NULL,
    `year` INT NOT NULL,
    `month` INT NOT NULL,
    sprint_or_period VARCHAR(50),
    delivery_date DATE NOT NULL,
    evolutivos_count INT NOT NULL DEFAULT 0,
    soportes_count INT NOT NULL DEFAULT 0,
    standard_change_count INT NOT NULL DEFAULT 0,
    notes TEXT,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(100),
    last_modified_by VARCHAR(100),
    updated_at DATETIME
);

CREATE TABLE client_returns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_delivery_metric_id BIGINT,
    ibl VARCHAR(50) NOT NULL,
    category VARCHAR(30) NOT NULL,
    root_cause TEXT NOT NULL,
    return_count INT NOT NULL DEFAULT 1,
    counted_in_quality BOOLEAN NOT NULL DEFAULT FALSE,
    return_date DATE NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(100),
    last_modified_by VARCHAR(100),
    updated_at DATETIME,
    CONSTRAINT fk_client_return_metric FOREIGN KEY (client_delivery_metric_id) REFERENCES client_delivery_metrics(id) ON DELETE CASCADE
);
