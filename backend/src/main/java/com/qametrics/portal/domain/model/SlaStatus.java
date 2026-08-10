package com.qametrics.portal.domain.model;

/**
 * Estado de cumplimiento de SLA para entregas.
 */
public enum SlaStatus {
    PENDING,  // Pendiente de entrega
    ON_TIME,  // Entregado a tiempo
    DELAYED   // Retrasado / Fuera de SLA
}
