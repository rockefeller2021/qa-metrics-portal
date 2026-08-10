package com.qametrics.portal.application.usecase;

import com.qametrics.portal.domain.model.*;
import com.qametrics.portal.domain.port.outbound.ClientTrackingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientTrackingUseCaseImplTest {

    @Mock
    private ClientTrackingRepository repository;

    @InjectMocks
    private ClientTrackingUseCaseImpl useCase;

    @Test
    @DisplayName("1ª Devolución IBL: debe registrarse con returnCount = 1 y countedInQuality = FALSE (no afecta calidad)")
    void createReturn_FirstTimeIBL_NotCountedInQuality() {
        ClientReturn input = new ClientReturn();
        input.setIbl("IBL-101");
        input.setCategory(ReturnCategory.EVOLUTIVO);
        input.setRootCause("Defecto de código");

        when(repository.countIblReturns("IBL-101")).thenReturn(0);
        when(repository.saveReturn(any(ClientReturn.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClientReturn result = useCase.createReturn(input);

        assertEquals(1, result.getReturnCount());
        assertFalse(result.isCountedInQuality(), "La 1ª devolución no debe contabilizarse en el % de calidad");
    }

    @Test
    @DisplayName("2ª Devolución IBL: debe registrarse con returnCount = 2 y countedInQuality = TRUE (sí afecta calidad)")
    void createReturn_SecondTimeIBL_CountedInQuality() {
        ClientReturn input = new ClientReturn();
        input.setIbl("IBL-101");
        input.setCategory(ReturnCategory.EVOLUTIVO);
        input.setRootCause("Defecto recurrente");

        when(repository.countIblReturns("IBL-101")).thenReturn(1);
        when(repository.saveReturn(any(ClientReturn.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClientReturn result = useCase.createReturn(input);

        assertEquals(2, result.getReturnCount());
        assertTrue(result.isCountedInQuality(), "La 2ª devolución en adelante SÍ debe contabilizarse en el % de calidad");
    }

    @Test
    @DisplayName("Cálculo de Calidad Target 95%: calcula correctamente los porcentajes por ítem y el consolidado")
    void getSummary_Calculates95TargetCorrectly() {
        ClientDeliveryMetric metric = new ClientDeliveryMetric();
        metric.setProjectType(ProjectType.FABRICA);
        metric.setYear(2026);
        metric.setMonth(8);
        metric.setEvolutivosCount(10);
        metric.setSoportesCount(10);
        metric.setStandardChangeCount(10);

        // 1 devolución para evolutivos (1ª vez -> false)
        ClientReturn r1 = new ClientReturn();
        r1.setIbl("IBL-100");
        r1.setCategory(ReturnCategory.EVOLUTIVO);
        r1.setReturnCount(1);
        r1.setCountedInQuality(false);

        // 1 devolución para evolutivos (2ª vez -> true)
        ClientReturn r2 = new ClientReturn();
        r2.setIbl("IBL-101");
        r2.setCategory(ReturnCategory.EVOLUTIVO);
        r2.setReturnCount(2);
        r2.setCountedInQuality(true);

        when(repository.findAllMetrics(ProjectType.FABRICA, 2026, 8)).thenReturn(List.of(metric));
        when(repository.findAllReturns(ProjectType.FABRICA, 2026, 8)).thenReturn(List.of(r1, r2));

        ClientTrackingSummary summary = useCase.getSummary(ProjectType.FABRICA, 2026, 8);

        assertEquals(10, summary.getTotalEvolutivos());
        assertEquals(1, summary.getDefectsEvolutivos()); // Solo r2 cuenta
        assertEquals(90.9, summary.getQualityEvolutivos()); // 10 / (10 + 1) = 90.9%

        assertEquals(100.0, summary.getQualitySoportes());
        assertEquals(100.0, summary.getQualityStandardChange());

        assertEquals(30, summary.getTotalDeliveries());
        assertEquals(1, summary.getTotalDefects());
        assertEquals(96.8, summary.getConsolidatedQuality()); // 30 / (30 + 1) = 96.77 -> 96.8%
    }
}
