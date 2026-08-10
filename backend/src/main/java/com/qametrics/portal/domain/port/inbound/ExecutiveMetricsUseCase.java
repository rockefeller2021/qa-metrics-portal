package com.qametrics.portal.domain.port.inbound;

import java.util.Map;

/**
 * Puerto de entrada (Inbound Port) para el Panel Ejecutivo de Calidad Consolidada.
 */
public interface ExecutiveMetricsUseCase {

    Map<String, Object> getExecutiveMetrics(String projectType, Integer year, Integer month);
}
