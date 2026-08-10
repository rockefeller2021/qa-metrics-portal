package com.qametrics.portal.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias del servicio de métricas de calidad.
 * Cobertura de las fórmulas definidas en el Documento Técnico.
 */
class QualityMetricServiceTest {

    private QualityMetricService service;

    @BeforeEach
    void setUp() {
        service = new QualityMetricService();
    }

    @Test
    @DisplayName("Debe calcular correctamente el % de Calidad alcanzando la meta del 95%")
    void shouldCalculateQualityPercentageCorrectly() {
        // Given
        long totalSuccessfulCases = 100;
        long totalBugsFound = 4; // (1 - 4/100) * 100 = 96%

        // When
        double qualityPercentage = service.calculateQuality(totalSuccessfulCases, totalBugsFound);

        // Then
        assertEquals(96.0, qualityPercentage);
        assertTrue(qualityPercentage >= 95.0, "La calidad debe superar el target del 95%");
    }

    @Test
    @DisplayName("Debe retornar 0% cuando no hay casos exitosos")
    void shouldReturnZeroWhenNoSuccessfulCases() {
        assertEquals(0.0, service.calculateQuality(0, 5));
    }

    @Test
    @DisplayName("Debe detectar cuando el target NO se alcanza (< 95%)")
    void shouldDetectTargetNotAchieved() {
        // (1 - 10/100) * 100 = 90%
        double quality = service.calculateQuality(100, 10);
        assertEquals(90.0, quality);
        assertFalse(service.isTargetAchieved(quality));
    }

    @Test
    @DisplayName("Debe retornar nivel ROJO cuando calidad < 90%")
    void shouldReturnRedAlertWhenQualityBelowNinety() {
        double quality = service.calculateQuality(100, 15); // 85%
        assertEquals(QualityMetricService.AlertLevel.RED, service.getAlertLevel(quality));
    }

    @Test
    @DisplayName("Debe retornar nivel AMARILLO cuando calidad está entre 90% y 94.99%")
    void shouldReturnYellowAlertWhenQualityBetweenNinetyAndTarget() {
        double quality = service.calculateQuality(100, 8); // 92%
        assertEquals(QualityMetricService.AlertLevel.YELLOW, service.getAlertLevel(quality));
    }

    @Test
    @DisplayName("Debe retornar nivel VERDE cuando calidad >= 95%")
    void shouldReturnGreenAlertWhenTargetAchieved() {
        double quality = service.calculateQuality(100, 2); // 98%
        assertEquals(QualityMetricService.AlertLevel.GREEN, service.getAlertLevel(quality));
    }

    @Test
    @DisplayName("Debe calcular el ratio de ejecución correctamente")
    void shouldCalculateExecutionRatioCorrectly() {
        double ratio = service.calculateExecutionRatio(80, 100);
        assertEquals(0.8, ratio);
    }

    @Test
    @DisplayName("Debe retornar ratio 0 cuando no hay intentos totales")
    void shouldReturnZeroRatioWhenNoAttempts() {
        assertEquals(0.0, service.calculateExecutionRatio(0, 0));
    }
}
