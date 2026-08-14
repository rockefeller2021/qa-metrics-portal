package com.qametrics.portal.infrastructure.adapters.in.rest;

import com.qametrics.portal.domain.model.ProjectType;
import com.qametrics.portal.domain.model.RunStatus;
import com.qametrics.portal.domain.port.outbound.BugRepository;
import com.qametrics.portal.domain.port.outbound.TestExecutionRepository;
import com.qametrics.portal.domain.service.QualityMetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Adaptador REST para el Motor de Indicadores y Métricas.
 * Calcula % Calidad, Ratio Ejecución y datos para el dashboard.
 */
@RestController
@RequestMapping("/metrics")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Métricas", description = "Motor de indicadores de calidad — Target 95%")
public class MetricsController {

    private final TestExecutionRepository executionRepository;
    private final BugRepository bugRepository;
    private final QualityMetricService metricService;

    public MetricsController(TestExecutionRepository executionRepository,
                              BugRepository bugRepository,
                              QualityMetricService metricService) {
        this.executionRepository = executionRepository;
        this.bugRepository = bugRepository;
        this.metricService = metricService;
    }

    @GetMapping("/quality")
    @Operation(summary = "% de Calidad por periodo y tipo de proyecto")
    public ResponseEntity<Map<String, Object>> getQuality(
            @RequestParam(required = false) ProjectType projectType,
            @RequestParam(required = false) String sprintOrPi,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        // Obtener datos de BD
        var executions = executionRepository.findAll(projectType, sprintOrPi, year, month);
        var bugs       = bugRepository.findAll(projectType, sprintOrPi, year, month);

        // Calcular métricas
        long totalAttempts  = executions.stream().mapToLong(e -> e.getRuns().size()).sum();
        long successfulCases= executions.stream()
                .flatMap(e -> e.getRuns().stream())
                .filter(r -> RunStatus.SUCCESSFUL.equals(r.getStatus()))
                .count();
        long bugsFound = bugs.size();

        double qualityPct    = metricService.calculateQuality(successfulCases, bugsFound);
        double executionRatio= metricService.calculateExecutionRatio(successfulCases, totalAttempts);
        boolean targetAchieved = metricService.isTargetAchieved(qualityPct);
        String alertLevel    = metricService.getAlertLevel(qualityPct).name();

        Map<String, Object> response = new HashMap<>();
        response.put("qualityPercentage", qualityPct);
        response.put("executionRatio", executionRatio);
        response.put("targetAchieved", targetAchieved);
        response.put("alertLevel", alertLevel);
        response.put("totalCases", totalAttempts);
        response.put("successfulCases", successfulCases);
        response.put("bugsFound", bugsFound);
        response.put("projectType", projectType != null ? projectType.name() : "ALL");
        response.put("sprintOrPi", sprintOrPi != null ? sprintOrPi : "ALL");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Resumen consolidado para el dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        Map<String, Object> dashboard = new HashMap<>();

        // Métricas consolidadas
        dashboard.put("fabrica", buildMetrics(ProjectType.FABRICA, year, month));
        dashboard.put("minorDemand", buildMetrics(ProjectType.MINOR_DEMAND, year, month));
        dashboard.put("consolidated", buildMetrics(null, year, month));
        dashboard.put("qualityTarget", QualityMetricService.QUALITY_TARGET);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/trend")
    @Operation(summary = "Tendencia mensual de calidad por tipo de proyecto para gráficos de barra ECharts")
    public ResponseEntity<java.util.List<Map<String, Object>>> getMonthlyTrend(
            @RequestParam(required = false) ProjectType projectType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        int targetYear;
        if (year != null) {
            targetYear = year;
        } else {
            var allExecs = executionRepository.findAll(projectType, null, null, null);
            targetYear = allExecs.stream()
                    .map(e -> e.getAssignmentDate() != null ? e.getAssignmentDate().getYear() : (e.getCreatedAt() != null ? e.getCreatedAt().getYear() : java.time.LocalDate.now().getYear()))
                    .max(Integer::compareTo)
                    .orElse(java.time.LocalDate.now().getYear());
        }

        java.util.List<Map<String, Object>> trend = new java.util.ArrayList<>();
        String[] monthNames = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

        int startMonth = (month != null && month >= 1 && month <= 12) ? month : 1;
        int endMonth   = (month != null && month >= 1 && month <= 12) ? month : 12;

        for (int m = startMonth; m <= endMonth; m++) {
            Map<String, Object> fabrica = buildMetrics(ProjectType.FABRICA, targetYear, m);
            Map<String, Object> minor   = buildMetrics(ProjectType.MINOR_DEMAND, targetYear, m);
            Map<String, Object> cons    = buildMetrics(projectType, targetYear, m);

            long fabricaExecs = ((Number) fabrica.get("totalCases")).longValue();
            long fabricaBugs  = ((Number) fabrica.get("bugsFound")).longValue();

            long minorExecs   = ((Number) minor.get("totalCases")).longValue();
            long minorBugs    = ((Number) minor.get("bugsFound")).longValue();

            long consExecs    = ((Number) cons.get("totalCases")).longValue();
            long consBugs     = ((Number) cons.get("bugsFound")).longValue();

            if (consExecs > 0 || consBugs > 0) {
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("monthName", monthNames[m - 1] + " " + targetYear);
                monthData.put("year", targetYear);
                monthData.put("month", m);

                // Si un tipo de proyecto no tiene registros en ese mes, se envía null para no graficar barra en 0
                monthData.put("fabricaQuality", (fabricaExecs > 0 || fabricaBugs > 0) ? fabrica.get("qualityPercentage") : null);
                monthData.put("minorDemandQuality", (minorExecs > 0 || minorBugs > 0) ? minor.get("qualityPercentage") : null);
                monthData.put("consolidatedQuality", cons.get("qualityPercentage"));

                monthData.put("successfulCases", cons.get("successfulCases"));
                monthData.put("bugsFound", cons.get("bugsFound"));
                monthData.put("targetQuality", 95.0);
                trend.add(monthData);
            }
        }
        return ResponseEntity.ok(trend);
    }

    private Map<String, Object> buildMetrics(ProjectType type, Integer year, Integer month) {
        var executions = executionRepository.findAll(type, null, year, month);
        var bugs       = bugRepository.findAll(type, null, year, month);

        // Suma total de ejecuciones realizadas (Run 1 + Retests)
        long totalExecutions = executions.stream()
                .mapToLong(e -> {
                    if (e.getRuns() != null && !e.getRuns().isEmpty()) {
                        long runSum = e.getRuns().stream().mapToLong(r -> r.getCasesExecuted() > 0 ? r.getCasesExecuted() : 1).sum();
                        return Math.max(runSum, Math.max(e.getTotalCases(), e.getRuns().size()));
                    }
                    return Math.max(e.getTotalCases(), 1);
                })
                .sum();

        long successfulCases = executions.stream()
                .mapToLong(e -> {
                    if (e.getSuccessfulCases() > 0) return e.getSuccessfulCases();
                    if (e.getRuns() != null && !e.getRuns().isEmpty()) {
                        long casesPassedSum = e.getRuns().stream().mapToLong(r -> r.getCasesPassed()).sum();
                        if (casesPassedSum > 0) return casesPassedSum;
                        return e.getRuns().stream().filter(r -> RunStatus.SUCCESSFUL.equals(r.getStatus())).count();
                    }
                    return 0;
                })
                .sum();

        long bugsFound = bugs.size();
        long reinjections = bugs.stream().filter(b -> b.isReinjectionFlag()).count();

        double quality = metricService.calculateQuality(successfulCases, bugsFound);
        double executionRatio = metricService.calculateExecutionRatio(successfulCases, totalExecutions);
        double executionsPerSuccess = metricService.calculateExecutionsPerSuccess(totalExecutions, successfulCases);

        Map<String, Object> res = new HashMap<>();
        res.put("qualityPercentage", quality);
        res.put("executionRatio", executionRatio);
        res.put("executionsPerSuccess", executionsPerSuccess);
        res.put("targetAchieved", metricService.isTargetAchieved(quality));
        res.put("alertLevel", metricService.getAlertLevel(quality).name());
        res.put("totalCases", totalExecutions);
        res.put("successfulCases", successfulCases);
        res.put("bugsFound", bugsFound);
        res.put("reinjections", reinjections);
        return res;
    }
}
