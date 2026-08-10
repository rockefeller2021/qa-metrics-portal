package com.qametrics.portal.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface BugJpaRepository extends JpaRepository<BugEntity, Long> {

    @Query("SELECT b FROM BugEntity b WHERE " +
           "(:projectType IS NULL OR b.projectType = :projectType) AND " +
           "(:sprintOrPi IS NULL OR b.sprintOrPi = :sprintOrPi) AND " +
           "(:year IS NULL OR YEAR(b.reportedDate) = :year) AND " +
           "(:month IS NULL OR MONTH(b.reportedDate) = :month)")
    List<BugEntity> findByFilters(
            @Param("projectType") String projectType,
            @Param("sprintOrPi") String sprintOrPi,
            @Param("year") Integer year,
            @Param("month") Integer month);

    @Query("SELECT b FROM BugEntity b WHERE b.reinjectionFlag = true AND " +
           "(:projectType IS NULL OR b.projectType = :projectType)")
    List<BugEntity> findReinjections(@Param("projectType") String projectType);

    @Query("SELECT COUNT(b) > 0 FROM BugEntity b WHERE b.requirementId = :reqId AND " +
           "b.status IN ('RESOLVED', 'CLOSED')")
    boolean existsByRequirementIdAndStatusResolved(@Param("reqId") String requirementId);
}
