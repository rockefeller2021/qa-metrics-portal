package com.qametrics.portal.application.usecase;

import com.qametrics.portal.domain.model.*;
import com.qametrics.portal.domain.port.outbound.BugRepository;
import com.qametrics.portal.domain.port.outbound.DeliverySlaRepository;
import com.qametrics.portal.domain.port.outbound.TestExecutionRepository;
import com.qametrics.portal.domain.service.QualityMetricService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutiveMetricsUseCaseImplTest {

    @Mock
    private TestExecutionRepository executionRepository;

    @Mock
    private BugRepository bugRepository;

    @Mock
    private DeliverySlaRepository deliverySlaRepository;

    @Spy
    private QualityMetricService metricService = new QualityMetricService();

    @InjectMocks
    private ExecutiveMetricsUseCaseImpl executiveMetricsUseCase;

    @Test
    @DisplayName("Obtener métricas ejecutivas filtrando por requestType EVOLUTIVO")
    void getExecutiveMetrics_WithRequestTypeFilter() {
        TestExecution exec = new TestExecution();
        exec.setProjectType(ProjectType.FABRICA);
        exec.setRequestType(RequestType.EVOLUTIVO);
        exec.setTotalCases(10);
        exec.setSuccessfulCases(10);

        Bug bug = new Bug();
        bug.setProjectType(ProjectType.FABRICA);
        bug.setRequestType(RequestType.EVOLUTIVO);
        bug.setStatus(BugStatus.OPEN);

        when(executionRepository.findAll(eq(ProjectType.FABRICA), eq(RequestType.EVOLUTIVO), eq(null), eq(2026), eq(8)))
                .thenReturn(List.of(exec));
        when(bugRepository.findAll(eq(ProjectType.FABRICA), eq(RequestType.EVOLUTIVO), eq(null), eq(2026), eq(8)))
                .thenReturn(List.of(bug));
        when(deliverySlaRepository.findAll(eq(ProjectType.FABRICA), eq(RequestType.EVOLUTIVO), eq(null), eq(null), eq(2026), eq(8)))
                .thenReturn(List.of());

        Map<String, Object> result = executiveMetricsUseCase.getExecutiveMetrics("FABRICA", 2026, 8, "EVOLUTIVO");

        assertNotNull(result);
        assertEquals(90.0, (Double) result.get("qualityTargetScore"), 0.1);
        assertEquals(10L, result.get("totalSuccessfulCases"));
        assertEquals(1L, result.get("totalBugs"));
    }
}
