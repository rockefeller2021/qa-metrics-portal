package com.qametrics.portal.application.usecase;

import com.qametrics.portal.domain.model.*;
import com.qametrics.portal.domain.port.inbound.ExecutiveMetricsUseCase;
import com.qametrics.portal.domain.port.outbound.BugRepository;
import com.qametrics.portal.domain.port.outbound.DeliverySlaRepository;
import com.qametrics.portal.domain.port.outbound.TestExecutionRepository;
import com.qametrics.portal.domain.service.QualityMetricService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Caso de Uso — Cálculo Consolidado de Métricas Ejecutivas (% Calidad Target 95%, Ratio & Bugs).
 * Utiliza de forma estricta QualityMetricService para garantizar consistencia total con el Dashboard.
 */
@Service
@Transactional(readOnly = true)
public class ExecutiveMetricsUseCaseImpl implements ExecutiveMetricsUseCase {

    private final TestExecutionRepository executionRepository;
    private final BugRepository bugRepository;
    private final DeliverySlaRepository deliverySlaRepository;
    private final QualityMetricService metricService;

    public ExecutiveMetricsUseCaseImpl(TestExecutionRepository executionRepository,
                                       BugRepository bugRepository,
                                       DeliverySlaRepository deliverySlaRepository,
                                       QualityMetricService metricService) {
        this.executionRepository = executionRepository;
        this.bugRepository = bugRepository;
        this.deliverySlaRepository = deliverySlaRepository;
        this.metricService = metricService;
    }

    @Override
    public Map<String, Object> getExecutiveMetrics(String projectTypeStr, Integer year, Integer month) {
        ProjectType pType = parseProjectType(projectTypeStr);

        List<TestExecution> executions = executionRepository.findAll(pType, null, year, month);
        List<Bug> bugs                   = bugRepository.findAll(pType, null, year, month);
        List<DeliverySla> deliveries     = deliverySlaRepository.findAll(pType, null, null, year, month);

        // 1. Conteo Unificado de Casos e Iteraciones (Run 1 + Retests N)
        long totalDesigned = executions.stream().mapToLong(TestExecution::getTotalCases).sum();

        long totalOk = executions.stream()
                .mapToLong(e -> {
                    if (e.getSuccessfulCases() > 0) return e.getSuccessfulCases();
                    if (e.getRuns() != null && !e.getRuns().isEmpty()) {
                        return e.getRuns().stream().filter(r -> RunStatus.SUCCESSFUL.equals(r.getStatus())).count();
                    }
                    return 0;
                })
                .sum();

        long totalFail = executions.stream().mapToLong(TestExecution::getFailedCases).sum();
        long totalBlock = executions.stream().mapToLong(TestExecution::getBlockedCases).sum();

        // Total de corridas/ejecuciones acumuladas (1 a N retests)
        long totalExecutions = executions.stream()
                .mapToLong(e -> {
                    if (e.getRuns() != null && !e.getRuns().isEmpty()) {
                        long runSum = e.getRuns().stream().mapToLong(r -> r.getCasesExecuted() > 0 ? r.getCasesExecuted() : 1).sum();
                        return Math.max(runSum, Math.max(e.getTotalCases(), e.getRuns().size()));
                    }
                    return Math.max(e.getTotalCases(), 1);
                })
                .sum();

        long totalBugs = bugs.size();

        // 2. Métrica % Calidad (Target 95%) unificada con QualityMetricService
        double qualityScore = metricService.calculateQuality(totalOk, totalBugs);
        boolean isTargetMet = metricService.isTargetAchieved(qualityScore);

        // 3. Ratio de Ejecución Real % (Unificado con QualityMetricService)
        double executionRatioDecimal = metricService.calculateExecutionRatio(totalOk, totalExecutions);
        double executionRatioPct = Math.round(executionRatioDecimal * 100.0 * 100.0) / 100.0;

        // 4. Esfuerzo de Ejecución (Exec / OK) unificado con QualityMetricService
        double executionEffortRaw = metricService.calculateExecutionsPerSuccess(totalExecutions, totalOk);
        double executionEffort = Math.round(executionEffortRaw * 100.0) / 100.0;

        // 5. BugTracker & Reinyecciones (RF03)
        long reinjections     = bugs.stream().filter(Bug::isReinjectionFlag).count();
        long openBugs         = bugs.stream().filter(b -> BugStatus.OPEN.equals(b.getStatus()) || BugStatus.IN_PROGRESS.equals(b.getStatus())).count();
        long resolvedBugs     = totalBugs - openBugs;
        double reinjectionRate = totalBugs > 0 ? ((double) reinjections / totalBugs) * 100.0 : 0.0;

        // 6. SLA Deliveries (RF04)
        long totalDeliveries = deliveries.size();
        long onTimeSla       = deliveries.stream().filter(d -> SlaStatus.ON_TIME.equals(d.getStatus())).count();
        long delayedSla      = deliveries.stream().filter(d -> SlaStatus.DELAYED.equals(d.getStatus())).count();
        double slaCompliance = totalDeliveries > 0 ? ((double) onTimeSla / totalDeliveries) * 100.0 : 100.0;

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("qualityTargetScore", qualityScore);
        metrics.put("isTargetMet", isTargetMet);
        metrics.put("executionRatioPct", executionRatioPct);
        metrics.put("executionEffort", executionEffort);

        metrics.put("totalDesignedCases", totalDesigned);
        metrics.put("totalSuccessfulCases", totalOk);
        metrics.put("totalFailedCases", totalFail);
        metrics.put("totalBlockedCases", totalBlock);
        metrics.put("totalExecutions", totalExecutions);

        metrics.put("totalBugs", totalBugs);
        metrics.put("reinjectionsCount", reinjections);
        metrics.put("openBugsCount", openBugs);
        metrics.put("resolvedBugsCount", resolvedBugs);
        metrics.put("reinjectionRatePct", Math.round(reinjectionRate * 100.0) / 100.0);

        metrics.put("totalDeliveries", totalDeliveries);
        metrics.put("onTimeSla", onTimeSla);
        metrics.put("delayedSla", delayedSla);
        metrics.put("slaComplianceRatio", Math.round(slaCompliance * 100.0) / 100.0);

        return metrics;
    }

    private ProjectType parseProjectType(String type) {
        if (type == null || type.isBlank()) return null;
        try {
            return ProjectType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
