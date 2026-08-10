package com.qametrics.portal.infrastructure.adapters.out.persistence;

import com.qametrics.portal.domain.model.*;
import com.qametrics.portal.domain.port.outbound.ClientTrackingRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ClientTrackingRepositoryAdapter implements ClientTrackingRepository {

    private final ClientDeliveryMetricJpaRepository metricRepo;
    private final ClientReturnJpaRepository returnRepo;

    public ClientTrackingRepositoryAdapter(ClientDeliveryMetricJpaRepository metricRepo,
                                           ClientReturnJpaRepository returnRepo) {
        this.metricRepo = metricRepo;
        this.returnRepo = returnRepo;
    }

    @Override
    public List<ClientDeliveryMetric> findAllMetrics(ProjectType projectType, Integer year, Integer month) {
        String pt = projectType != null ? projectType.name() : null;
        return metricRepo.findByFilters(pt, year, month)
                .stream().map(this::metricToDomain).toList();
    }

    @Override
    public Optional<ClientDeliveryMetric> findMetricById(Long id) {
        return metricRepo.findById(id).map(this::metricToDomain);
    }

    @Override
    public Optional<ClientDeliveryMetric> findMetricByPeriod(ProjectType projectType, int year, int month, String sprintOrPeriod) {
        String pt = projectType != null ? projectType.name() : "FABRICA";
        List<ClientDeliveryMetricEntity> list = metricRepo.findByPeriod(pt, year, month, sprintOrPeriod);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(metricToDomain(list.get(0)));
    }

    @Override
    public ClientDeliveryMetric saveMetric(ClientDeliveryMetric metric) {
        return metricToDomain(metricRepo.save(metricToEntity(metric)));
    }

    @Override
    public void deleteMetricById(Long id) {
        metricRepo.deleteById(id);
    }

    @Override
    public void deleteAllMetrics() {
        metricRepo.deleteAll();
    }

    @Override
    public List<ClientReturn> findAllReturns(ProjectType projectType, Integer year, Integer month) {
        String pt = projectType != null ? projectType.name() : null;
        return returnRepo.findByFilters(pt, year, month)
                .stream().map(this::returnToDomain).toList();
    }

    @Override
    public Optional<ClientReturn> findReturnById(Long id) {
        return returnRepo.findById(id).map(this::returnToDomain);
    }

    @Override
    public ClientReturn saveReturn(ClientReturn clientReturn) {
        ClientDeliveryMetricEntity parent = null;
        if (clientReturn.getClientDeliveryMetricId() != null) {
            parent = metricRepo.findById(clientReturn.getClientDeliveryMetricId()).orElse(null);
        }
        ClientReturnEntity entity = returnToEntity(clientReturn, parent);
        return returnToDomain(returnRepo.save(entity));
    }

    @Override
    public void deleteReturnById(Long id) {
        returnRepo.deleteById(id);
    }

    @Override
    public int countIblReturns(String ibl) {
        return returnRepo.countByIbl(ibl);
    }

    // ── Mapeos ──────────────────────────────────────────────
    private ClientDeliveryMetric metricToDomain(ClientDeliveryMetricEntity e) {
        ClientDeliveryMetric m = new ClientDeliveryMetric();
        m.setId(e.getId());
        m.setProjectType(ProjectType.valueOf(e.getProjectType()));
        m.setYear(e.getYear());
        m.setMonth(e.getMonth());
        m.setSprintOrPeriod(e.getSprintOrPeriod());
        m.setDeliveryDate(e.getDeliveryDate());
        m.setEvolutivosCount(e.getEvolutivosCount());
        m.setSoportesCount(e.getSoportesCount());
        m.setStandardChangeCount(e.getStandardChangeCount());
        m.setNotes(e.getNotes());
        m.setCreatedAt(e.getCreatedAt());
        m.setCreatedBy(e.getCreatedBy());
        m.setLastModifiedBy(e.getLastModifiedBy());
        m.setUpdatedAt(e.getUpdatedAt());

        if (e.getReturns() != null) {
            m.setReturns(e.getReturns().stream().map(this::returnToDomain).toList());
        }
        return m;
    }

    private ClientDeliveryMetricEntity metricToEntity(ClientDeliveryMetric m) {
        ClientDeliveryMetricEntity e = new ClientDeliveryMetricEntity();
        if (m.getId() != null) e.setId(m.getId());
        e.setProjectType(m.getProjectType().name());
        e.setYear(m.getYear());
        e.setMonth(m.getMonth());
        e.setSprintOrPeriod(m.getSprintOrPeriod());
        e.setDeliveryDate(m.getDeliveryDate());
        e.setEvolutivosCount(m.getEvolutivosCount());
        e.setSoportesCount(m.getSoportesCount());
        e.setStandardChangeCount(m.getStandardChangeCount());
        e.setNotes(m.getNotes());
        if (m.getCreatedAt() != null) e.setCreatedAt(m.getCreatedAt());
        if (m.getCreatedBy() != null) e.setCreatedBy(m.getCreatedBy());
        if (m.getLastModifiedBy() != null) e.setLastModifiedBy(m.getLastModifiedBy());
        if (m.getUpdatedAt() != null) e.setUpdatedAt(m.getUpdatedAt());

        if (m.getReturns() != null && !m.getReturns().isEmpty()) {
            List<ClientReturnEntity> returnEntities = m.getReturns().stream()
                    .map(r -> returnToEntity(r, e))
                    .toList();
            e.setReturns(returnEntities);
        }
        return e;
    }

    private ClientReturn returnToDomain(ClientReturnEntity r) {
        ClientReturn cr = new ClientReturn();
        cr.setId(r.getId());
        cr.setClientDeliveryMetricId(r.getClientDeliveryMetric() != null ? r.getClientDeliveryMetric().getId() : null);
        if (r.getProjectType() != null) {
            cr.setProjectType(ProjectType.valueOf(r.getProjectType()));
        } else if (r.getClientDeliveryMetric() != null && r.getClientDeliveryMetric().getProjectType() != null) {
            cr.setProjectType(ProjectType.valueOf(r.getClientDeliveryMetric().getProjectType()));
        } else {
            cr.setProjectType(ProjectType.FABRICA);
        }
        cr.setYear(r.getYear() > 0 ? r.getYear() : (r.getReturnDate() != null ? r.getReturnDate().getYear() : java.time.LocalDate.now().getYear()));
        cr.setMonth(r.getMonth() > 0 ? r.getMonth() : (r.getReturnDate() != null ? r.getReturnDate().getMonthValue() : java.time.LocalDate.now().getMonthValue()));
        cr.setIbl(r.getIbl());
        cr.setCategory(ReturnCategory.valueOf(r.getCategory()));
        cr.setRootCause(r.getRootCause());
        cr.setReturnCount(r.getReturnCount());
        cr.setCountedInQuality(r.isCountedInQuality());
        cr.setReturnDate(r.getReturnDate());
        cr.setCreatedAt(r.getCreatedAt());
        cr.setCreatedBy(r.getCreatedBy());
        cr.setLastModifiedBy(r.getLastModifiedBy());
        cr.setUpdatedAt(r.getUpdatedAt());
        return cr;
    }

    private ClientReturnEntity returnToEntity(ClientReturn r, ClientDeliveryMetricEntity parent) {
        ClientReturnEntity e = new ClientReturnEntity();
        if (r.getId() != null) e.setId(r.getId());
        e.setClientDeliveryMetric(parent);
        e.setProjectType(r.getProjectType() != null ? r.getProjectType().name() : (parent != null ? parent.getProjectType() : "FABRICA"));
        e.setYear(r.getYear() > 0 ? r.getYear() : (r.getReturnDate() != null ? r.getReturnDate().getYear() : java.time.LocalDate.now().getYear()));
        e.setMonth(r.getMonth() > 0 ? r.getMonth() : (r.getReturnDate() != null ? r.getReturnDate().getMonthValue() : java.time.LocalDate.now().getMonthValue()));
        e.setIbl(r.getIbl());
        e.setCategory(r.getCategory().name());
        e.setRootCause(r.getRootCause());
        e.setReturnCount(r.getReturnCount());
        e.setCountedInQuality(r.isCountedInQuality());
        e.setReturnDate(r.getReturnDate());
        if (r.getCreatedAt() != null) e.setCreatedAt(r.getCreatedAt());
        if (r.getCreatedBy() != null) e.setCreatedBy(r.getCreatedBy());
        if (r.getLastModifiedBy() != null) e.setLastModifiedBy(r.getLastModifiedBy());
        if (r.getUpdatedAt() != null) e.setUpdatedAt(r.getUpdatedAt());
        return e;
    }
}
