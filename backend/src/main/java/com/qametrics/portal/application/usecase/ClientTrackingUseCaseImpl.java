package com.qametrics.portal.application.usecase;

import com.qametrics.portal.domain.model.*;
import com.qametrics.portal.domain.port.inbound.ClientTrackingUseCase;
import com.qametrics.portal.domain.port.outbound.ClientTrackingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class ClientTrackingUseCaseImpl implements ClientTrackingUseCase {

    private final ClientTrackingRepository repository;

    public ClientTrackingUseCaseImpl(ClientTrackingRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientDeliveryMetric> findAllMetrics(ProjectType projectType, Integer year, Integer month) {
        return repository.findAllMetrics(projectType, year, month);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClientDeliveryMetric> findMetricById(Long id) {
        return repository.findMetricById(id);
    }

    @Override
    public ClientDeliveryMetric createMetric(ClientDeliveryMetric metric) {
        if (metric.getYear() <= 0) {
            metric.setYear(metric.getDeliveryDate() != null ? metric.getDeliveryDate().getYear() : LocalDate.now().getYear());
        }
        if (metric.getMonth() <= 0) {
            metric.setMonth(metric.getDeliveryDate() != null ? metric.getDeliveryDate().getMonthValue() : LocalDate.now().getMonthValue());
        }
        if (metric.getDeliveryDate() == null || metric.getDeliveryDate().getMonthValue() != metric.getMonth() || metric.getDeliveryDate().getYear() != metric.getYear()) {
            metric.setDeliveryDate(LocalDate.of(metric.getYear(), Math.max(1, Math.min(12, metric.getMonth())), 1));
        }

        // Regla de Negocio: Sobrescribir registro existente para el mismo periodo/semana y línea de proyecto
        Optional<ClientDeliveryMetric> existing = repository.findMetricByPeriod(
                metric.getProjectType(),
                metric.getYear(),
                metric.getMonth(),
                metric.getSprintOrPeriod()
        );

        if (existing.isPresent()) {
            ClientDeliveryMetric toUpdate = existing.get();
            toUpdate.setEvolutivosCount(metric.getEvolutivosCount());
            toUpdate.setSoportesCount(metric.getSoportesCount());
            toUpdate.setStandardChangeCount(metric.getStandardChangeCount());
            toUpdate.setDeliveryDate(metric.getDeliveryDate());
            if (metric.getNotes() != null && !metric.getNotes().isBlank()) {
                toUpdate.setNotes(metric.getNotes());
            }
            return repository.saveMetric(toUpdate);
        }

        return repository.saveMetric(metric);
    }

    @Override
    public ClientDeliveryMetric updateMetric(Long id, ClientDeliveryMetric metric) {
        repository.findMetricById(id).orElseThrow(
                () -> new IllegalArgumentException("Métrica de entrega no encontrada con ID: " + id));
        metric.setId(id);
        return repository.saveMetric(metric);
    }

    @Override
    public void deleteMetric(Long id) {
        repository.deleteMetricById(id);
    }

    @Override
    public void deleteAllMetrics() {
        repository.deleteAllMetrics();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientReturn> findAllReturns(ProjectType projectType, Integer year, Integer month) {
        return repository.findAllReturns(projectType, year, month);
    }

    @Override
    public ClientReturn createReturn(ClientReturn clientReturn) {
        if (clientReturn.getIbl() == null || clientReturn.getIbl().isBlank()) {
            throw new IllegalArgumentException("El código IBL es obligatorio para registrar la devolución.");
        }
        String cleanIbl = clientReturn.getIbl().trim().toUpperCase();
        clientReturn.setIbl(cleanIbl);

        if (clientReturn.getYear() <= 0) {
            clientReturn.setYear(clientReturn.getReturnDate() != null ? clientReturn.getReturnDate().getYear() : LocalDate.now().getYear());
        }
        if (clientReturn.getMonth() <= 0) {
            clientReturn.setMonth(clientReturn.getReturnDate() != null ? clientReturn.getReturnDate().getMonthValue() : LocalDate.now().getMonthValue());
        }
        if (clientReturn.getReturnDate() == null) {
            clientReturn.setReturnDate(LocalDate.of(clientReturn.getYear(), Math.max(1, Math.min(12, clientReturn.getMonth())), 1));
        }
        if (clientReturn.getProjectType() == null) {
            clientReturn.setProjectType(ProjectType.FABRICA);
        }

        // Regla de Negocio Crítica:
        // Contar cuántas devoluciones previas existen para este IBL
        int previousReturns = repository.countIblReturns(cleanIbl);
        int currentReturnNumber = previousReturns + 1;
        clientReturn.setReturnCount(currentReturnNumber);

        // 1ª vez -> NO cuenta como defecto para calidad (countedInQuality = false)
        // 2ª vez en adelante -> SÍ cuenta como defecto reincidente (countedInQuality = true)
        clientReturn.setCountedInQuality(currentReturnNumber >= 2);

        return repository.saveReturn(clientReturn);
    }

    @Override
    public void deleteReturn(Long id) {
        repository.deleteReturnById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientTrackingSummary getSummary(ProjectType projectType, Integer year, Integer month) {
        List<ClientDeliveryMetric> metrics = repository.findAllMetrics(projectType, year, month);
        List<ClientReturn> returns = repository.findAllReturns(projectType, year, month);

        ClientTrackingSummary summary = new ClientTrackingSummary();

        int totalEvo = metrics.stream().mapToInt(ClientDeliveryMetric::getEvolutivosCount).sum();
        int totalSop = metrics.stream().mapToInt(ClientDeliveryMetric::getSoportesCount).sum();
        int totalStd = metrics.stream().mapToInt(ClientDeliveryMetric::getStandardChangeCount).sum();

        // Contabilizar solo las devoluciones reincidentes (returnCount >= 2 / countedInQuality == true)
        int defEvo = (int) returns.stream()
                .filter(r -> ReturnCategory.EVOLUTIVO.equals(r.getCategory()) && r.isCountedInQuality())
                .count();

        int defSop = (int) returns.stream()
                .filter(r -> ReturnCategory.SOPORTE.equals(r.getCategory()) && r.isCountedInQuality())
                .count();

        int defStd = (int) returns.stream()
                .filter(r -> ReturnCategory.STANDARD_CHANGE.equals(r.getCategory()) && r.isCountedInQuality())
                .count();

        summary.setTotalEvolutivos(totalEvo);
        summary.setDefectsEvolutivos(defEvo);
        summary.setQualityEvolutivos(calculateQualityPercent(totalEvo, defEvo));

        summary.setTotalSoportes(totalSop);
        summary.setDefectsSoportes(defSop);
        summary.setQualitySoportes(calculateQualityPercent(totalSop, defSop));

        summary.setTotalStandardChange(totalStd);
        summary.setDefectsStandardChange(defStd);
        summary.setQualityStandardChange(calculateQualityPercent(totalStd, defStd));

        int grandTotalDeliveries = totalEvo + totalSop + totalStd;
        int grandTotalDefects = defEvo + defSop + defStd;

        summary.setTotalDeliveries(grandTotalDeliveries);
        summary.setTotalDefects(grandTotalDefects);
        summary.setConsolidatedQuality(calculateQualityPercent(grandTotalDeliveries, grandTotalDefects));

        // Construir gráfica de tendencia mensual (últimos 6 u 8 meses o por el año seleccionado)
        summary.setMonthlyTrend(buildMonthlyTrend(projectType, year != null ? year : LocalDate.now().getYear()));

        return summary;
    }

    private Double calculateQualityPercent(int totalDeliveries, int defectsCounted) {
        if (totalDeliveries <= 0) return null;
        int denominator = totalDeliveries + defectsCounted;
        if (denominator <= 0) return null;
        double ratio = (double) totalDeliveries / denominator;
        return Math.max(0.0, Math.round(ratio * 1000.0) / 10.0);
    }

    private List<ClientTrackingSummary.MonthlyQualityData> buildMonthlyTrend(ProjectType projectType, int targetYear) {
        List<ClientTrackingSummary.MonthlyQualityData> trend = new ArrayList<>();
        String[] monthNames = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

        for (int m = 1; m <= 12; m++) {
            List<ClientDeliveryMetric> mMetrics = repository.findAllMetrics(projectType, targetYear, m);
            List<ClientReturn> mReturns = repository.findAllReturns(projectType, targetYear, m);

            int evo = mMetrics.stream().mapToInt(ClientDeliveryMetric::getEvolutivosCount).sum();
            int sop = mMetrics.stream().mapToInt(ClientDeliveryMetric::getSoportesCount).sum();
            int std = mMetrics.stream().mapToInt(ClientDeliveryMetric::getStandardChangeCount).sum();

            int defEvo = (int) mReturns.stream().filter(r -> ReturnCategory.EVOLUTIVO.equals(r.getCategory()) && r.isCountedInQuality()).count();
            int defSop = (int) mReturns.stream().filter(r -> ReturnCategory.SOPORTE.equals(r.getCategory()) && r.isCountedInQuality()).count();
            int defStd = (int) mReturns.stream().filter(r -> ReturnCategory.STANDARD_CHANGE.equals(r.getCategory()) && r.isCountedInQuality()).count();

            int totalDel = evo + sop + std;
            int totalDef = defEvo + defSop + defStd;

            Double qEvo = calculateQualityPercent(evo, defEvo);
            Double qSop = calculateQualityPercent(sop, defSop);
            Double qStd = calculateQualityPercent(std, defStd);
            Double qCons = calculateQualityPercent(totalDel, totalDef);

            // Incluir en el gráfico solo si existen datos para ese mes
            if (totalDel > 0 || mReturns.size() > 0) {
                trend.add(new ClientTrackingSummary.MonthlyQualityData(
                        monthNames[m - 1] + " " + targetYear,
                        targetYear,
                        m,
                        qEvo,
                        qSop,
                        qStd,
                        qCons
                ));
            }
        }
        return trend;
    }
}
