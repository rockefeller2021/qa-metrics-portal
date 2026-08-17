package com.qametrics.portal.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface TestExecutionJpaRepository extends JpaRepository<TestExecutionEntity, Long> {

    @Query("SELECT DISTINCT e FROM TestExecutionEntity e LEFT JOIN e.runs r WHERE " +
           "(:projectType IS NULL OR e.projectType = :projectType) AND " +
           "(:requestType IS NULL OR e.requestType = :requestType) AND " +
           "(:sprintOrPi IS NULL OR e.sprintOrPi = :sprintOrPi) AND " +
           "(:year IS NULL OR YEAR(e.assignmentDate) = :year OR YEAR(e.designDate) = :year OR (r IS NOT NULL AND YEAR(r.executionDate) = :year)) AND " +
           "(:month IS NULL OR MONTH(e.assignmentDate) = :month OR MONTH(e.designDate) = :month OR (r IS NOT NULL AND MONTH(r.executionDate) = :month))")
    List<TestExecutionEntity> findByFilters(
            @Param("projectType") String projectType,
            @Param("requestType") String requestType,
            @Param("sprintOrPi") String sprintOrPi,
            @Param("year") Integer year,
            @Param("month") Integer month);

    @Query("SELECT COUNT(r) + 1 FROM TestExecutionRunEntity r WHERE r.testExecution.id = :execId")
    int countNextRunNumber(@Param("execId") Long execId);
}
