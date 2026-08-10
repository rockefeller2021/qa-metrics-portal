package com.qametrics.portal.domain.service;

import org.springframework.stereotype.Component;

/**
 * Servicio de dominio — Fórmulas de Indicadores de Calidad.
 * Implementa las métricas definidas en el Documento Técnico.
 *
 * % Calidad = (1 - (Bugs / Casos Exitosos)) × 100   — Target: 95%
 * Ratio Ejecución = Casos Exitosos / Total Intentos
 */
@Component
public class QualityMetricService {

    public static final double QUALITY_TARGET = 95.0;

    /**
     * Calcula el % de Calidad.
     * @param successfulCases total de casos exitosos en el periodo
     * @param bugsFound       total de bugs encontrados en el periodo
     * @return porcentaje de calidad (0–100)
     */
    public double calculateQuality(long successfulCases, long bugsFound) {
        if (successfulCases <= 0) return 0.0;
        double quality = (1.0 - ((double) bugsFound / successfulCases)) * 100.0;
        return Math.max(0.0, Math.round(quality * 100.0) / 100.0);
    }

    /**
     * Calcula el Ratio de Ejecución.
     * @param successfulCases casos exitosos
     * @param totalAttempts   total de intentos de ejecución
     * @return ratio entre 0 y 1
     */
    public double calculateExecutionRatio(long successfulCases, long totalAttempts) {
        if (totalAttempts <= 0) return 0.0;
        return Math.round(((double) successfulCases / totalAttempts) * 10000.0) / 10000.0;
    }

    /**
     * Métrica de Esfuerzo: Ejecuciones requeridas por cada Caso Exitoso = Total Intentos / Casos Exitosos
     */
    public double calculateExecutionsPerSuccess(long totalAttempts, long successfulCases) {
        if (successfulCases == 0) return 0.0;
        return (double) totalAttempts / successfulCases;
    }

    /**
     * Determina si el % de calidad supera el target del 95%.
     */
    public boolean isTargetAchieved(double qualityPercentage) {
        return qualityPercentage >= QUALITY_TARGET;
    }

    /**
     * Retorna el nivel de alerta según el % de calidad.
     * GREEN ≥ 95%, YELLOW ≥ 90%, RED < 90%
     */
    public AlertLevel getAlertLevel(double qualityPercentage) {
        if (qualityPercentage >= 95.0) return AlertLevel.GREEN;
        if (qualityPercentage >= 90.0) return AlertLevel.YELLOW;
        return AlertLevel.RED;
    }

    public enum AlertLevel { GREEN, YELLOW, RED }
}
