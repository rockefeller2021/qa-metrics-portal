package com.qametrics.portal.domain.port.outbound;

import com.qametrics.portal.domain.model.Bug;
import com.qametrics.portal.domain.model.ProjectType;
import com.qametrics.portal.domain.model.RequestType;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia del BugTracker.
 */
public interface BugRepository {
    List<Bug> findAll(ProjectType projectType, String sprintOrPi, Integer year, Integer month);
    List<Bug> findAll(ProjectType projectType, RequestType requestType, String sprintOrPi, Integer year, Integer month);
    Optional<Bug> findById(Long id);
    Bug save(Bug bug);
    void deleteById(Long id);
    void deleteAll();
    void deleteByIds(List<Long> ids);
    List<Bug> findReinjections(ProjectType projectType);
    boolean existsByRequirementIdAndStatusResolved(String requirementId);
}
