package com.qametrics.portal.infrastructure.adapters.out.persistence;

import com.qametrics.portal.domain.model.*;
import com.qametrics.portal.domain.port.outbound.BugRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BugRepositoryAdapter implements BugRepository {

    private final BugJpaRepository jpaRepository;

    public BugRepositoryAdapter(BugJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Bug> findAll(ProjectType projectType, String sprintOrPi, Integer year, Integer month) {
        return findAll(projectType, null, sprintOrPi, year, month);
    }

    @Override
    public List<Bug> findAll(ProjectType projectType, RequestType requestType, String sprintOrPi, Integer year, Integer month) {
        String pt = (projectType != null) ? projectType.name() : null;
        String rt = (requestType != null) ? requestType.name() : null;
        return jpaRepository.findByFilters(pt, rt, sprintOrPi, year, month)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Bug> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Bug save(Bug bug) {
        return toDomain(jpaRepository.save(toEntity(bug)));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        jpaRepository.deleteAllById(ids);
    }

    @Override
    public List<Bug> findReinjections(ProjectType projectType) {
        String pt = (projectType != null) ? projectType.name() : null;
        return jpaRepository.findReinjections(pt)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByRequirementIdAndStatusResolved(String requirementId) {
        return jpaRepository.existsByRequirementIdAndStatusResolved(requirementId);
    }

    // ── Mapeos ──────────────────────────────────────────────
    private Bug toDomain(BugEntity e) {
        Bug b = new Bug();
        b.setId(e.getId());
        b.setBugJiraId(e.getBugJiraId());
        b.setRequirementId(e.getRequirementId());
        b.setProjectType(ProjectType.valueOf(e.getProjectType()));
        if (e.getRequestType() != null) {
            try { b.setRequestType(RequestType.valueOf(e.getRequestType())); } catch (Exception ex) { b.setRequestType(RequestType.EVOLUTIVO); }
        }
        b.setSprintOrPi(e.getSprintOrPi());
        b.setStatus(BugStatus.valueOf(e.getStatus()));
        b.setDefectType(DefectType.valueOf(e.getDefectType()));
        b.setDescription(e.getDescription());
        b.setReinjectionFlag(e.isReinjectionFlag());
        b.setReportedDate(e.getReportedDate());
        b.setResolvedDate(e.getResolvedDate());
        b.setReportedBy(e.getReportedBy());
        b.setDeveloperName(e.getDeveloperName());
        b.setCreatedAt(e.getCreatedAt());
        b.setCreatedBy(e.getCreatedBy());
        b.setLastModifiedBy(e.getLastModifiedBy());
        b.setUpdatedAt(e.getUpdatedAt());
        return b;
    }

    private BugEntity toEntity(Bug b) {
        BugEntity e = new BugEntity();
        if (b.getId() != null) e.setId(b.getId());
        e.setBugJiraId(b.getBugJiraId());
        e.setRequirementId(b.getRequirementId());
        e.setProjectType(b.getProjectType().name());
        if (b.getRequestType() != null) e.setRequestType(b.getRequestType().name());
        e.setSprintOrPi(b.getSprintOrPi());
        e.setStatus(b.getStatus().name());
        e.setDefectType(b.getDefectType().name());
        e.setDescription(b.getDescription());
        e.setReinjectionFlag(b.isReinjectionFlag());
        e.setReportedDate(b.getReportedDate());
        e.setResolvedDate(b.getResolvedDate());
        e.setReportedBy(b.getReportedBy());
        e.setDeveloperName(b.getDeveloperName());
        if (b.getCreatedAt() != null) e.setCreatedAt(b.getCreatedAt());
        if (b.getCreatedBy() != null) e.setCreatedBy(b.getCreatedBy());
        if (b.getLastModifiedBy() != null) e.setLastModifiedBy(b.getLastModifiedBy());
        if (b.getUpdatedAt() != null) e.setUpdatedAt(b.getUpdatedAt());
        return e;
    }
}
