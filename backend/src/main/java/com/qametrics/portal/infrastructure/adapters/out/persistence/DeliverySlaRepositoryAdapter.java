package com.qametrics.portal.infrastructure.adapters.out.persistence;

import com.qametrics.portal.domain.model.DeliverySla;
import com.qametrics.portal.domain.model.ProjectType;
import com.qametrics.portal.domain.model.SlaStatus;
import com.qametrics.portal.domain.port.outbound.DeliverySlaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DeliverySlaRepositoryAdapter implements DeliverySlaRepository {

    private final SpringDataDeliverySlaRepository repository;

    public DeliverySlaRepositoryAdapter(SpringDataDeliverySlaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DeliverySla> findAll(ProjectType projectType, SlaStatus status, String sprintOrPi, Integer year, Integer month) {
        return findAll(projectType, null, status, sprintOrPi, year, month);
    }

    @Override
    public List<DeliverySla> findAll(ProjectType projectType, com.qametrics.portal.domain.model.RequestType requestType, SlaStatus status, String sprintOrPi, Integer year, Integer month) {
        String typeStr   = (projectType != null) ? projectType.name() : null;
        String reqStr    = (requestType != null) ? requestType.name() : null;
        String statusStr = (status != null) ? status.name() : null;
        return repository.findAllFiltered(typeStr, reqStr, statusStr, sprintOrPi, year, month).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<DeliverySla> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public DeliverySla save(DeliverySla domain) {
        DeliverySlaEntity entity = toEntity(domain);
        DeliverySlaEntity saved  = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        repository.deleteAllById(ids);
    }

    // ── Mappers ──────────────────────────────────────────────
    private DeliverySla toDomain(DeliverySlaEntity entity) {
        DeliverySla model = new DeliverySla();
        model.setId(entity.getId());
        model.setJiraId(entity.getJiraId());
        model.setProjectType(ProjectType.valueOf(entity.getProjectType()));
        if (entity.getRequestType() != null) {
            try { model.setRequestType(com.qametrics.portal.domain.model.RequestType.valueOf(entity.getRequestType())); } catch (Exception ex) { model.setRequestType(com.qametrics.portal.domain.model.RequestType.EVOLUTIVO); }
        }
        model.setSprintOrPi(entity.getSprintOrPi());
        model.setDesignerAnalyst(entity.getDesignerAnalyst());
        model.setEstimatedDeliveryDate(entity.getEstimatedDeliveryDate());
        model.setEstimatedQaDate(entity.getEstimatedQaDate());
        model.setRealQaDate(entity.getRealQaDate());
        model.setRealClientDeliveryDate(entity.getRealClientDeliveryDate());
        model.setStatus(SlaStatus.valueOf(entity.getStatus()));
        model.setDelayDays(entity.getDelayDays());
        model.setNotes(entity.getNotes());
        model.setCreatedAt(entity.getCreatedAt());
        model.setCreatedBy(entity.getCreatedBy());
        model.setLastModifiedBy(entity.getLastModifiedBy());
        model.setUpdatedAt(entity.getUpdatedAt());
        return model;
    }

    private DeliverySlaEntity toEntity(DeliverySla domain) {
        DeliverySlaEntity entity = new DeliverySlaEntity();
        entity.setId(domain.getId());
        entity.setJiraId(domain.getJiraId());
        entity.setProjectType(domain.getProjectType().name());
        if (domain.getRequestType() != null) entity.setRequestType(domain.getRequestType().name());
        entity.setSprintOrPi(domain.getSprintOrPi());
        entity.setDesignerAnalyst(domain.getDesignerAnalyst());
        entity.setEstimatedDeliveryDate(domain.getEstimatedDeliveryDate());
        entity.setEstimatedQaDate(domain.getEstimatedQaDate());
        entity.setRealQaDate(domain.getRealQaDate());
        entity.setRealClientDeliveryDate(domain.getRealClientDeliveryDate());
        entity.setStatus(domain.getStatus().name());
        entity.setDelayDays(domain.getDelayDays());
        entity.setNotes(domain.getNotes());
        if (domain.getCreatedAt() != null) {
            entity.setCreatedAt(domain.getCreatedAt());
        }
        if (domain.getCreatedBy() != null) entity.setCreatedBy(domain.getCreatedBy());
        if (domain.getLastModifiedBy() != null) entity.setLastModifiedBy(domain.getLastModifiedBy());
        if (domain.getUpdatedAt() != null) entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
