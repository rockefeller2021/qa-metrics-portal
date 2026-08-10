package com.qametrics.portal.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface TestExecutionRunJpaRepository extends JpaRepository<TestExecutionRunEntity, Long> {

    @Query("SELECT r FROM TestExecutionRunEntity r WHERE r.testExecution.id = :execId ORDER BY r.runNumber")
    List<TestExecutionRunEntity> findByTestExecutionId(@Param("execId") Long execId);
}
