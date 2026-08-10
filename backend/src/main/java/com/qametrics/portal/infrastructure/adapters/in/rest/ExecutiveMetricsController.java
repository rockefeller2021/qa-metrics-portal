package com.qametrics.portal.infrastructure.adapters.in.rest;

import com.qametrics.portal.domain.port.inbound.ExecutiveMetricsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Adaptador REST para el Panel Ejecutivo de Calidad Consolidada (Quality Target 95%, Ratio & Bugs).
 */
@RestController
@RequestMapping("/metrics")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Métricas Ejecutivas", description = "Consolidado global de Calidad (95% Target), Ratio y Seguimiento de Bugs")
public class ExecutiveMetricsController {

    private final ExecutiveMetricsUseCase executiveMetricsUseCase;

    public ExecutiveMetricsController(ExecutiveMetricsUseCase executiveMetricsUseCase) {
        this.executiveMetricsUseCase = executiveMetricsUseCase;
    }

    @GetMapping("/executive")
    @Operation(summary = "Obtener consolidado de métricas ejecutivas", description = "Calcula la calidad oficial [1-(Bugs/OK)]*100, Ratio %, Esfuerzo y BugTracker")
    public ResponseEntity<Map<String, Object>> getExecutiveMetrics(
            @RequestParam(required = false) String projectType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(executiveMetricsUseCase.getExecutiveMetrics(projectType, year, month));
    }
}
