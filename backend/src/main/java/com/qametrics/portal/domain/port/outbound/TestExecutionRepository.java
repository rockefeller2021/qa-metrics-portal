package com.qametrics.portal.domain.port.outbound;

import com.qametrics.portal.domain.model.ExecutionRun;
import com.qametrics.portal.domain.model.ProjectType;
import com.qametrics.portal.domain.model.RequestType;
import com.qametrics.portal.domain.model.TestExecution;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistencia de Ejecuciones y Retests.
 */
public interface TestExecutionRepository {
    List<TestExecution> findAll(ProjectType projectType, String sprintOrPi, Integer year, Integer month);
    List<TestExecution> findAll(ProjectType projectType, RequestType requestType, String sprintOrPi, Integer year, Integer month);
    Optional<TestExecution> findById(Long id);
    TestExecution save(TestExecution execution);
    void deleteById(Long id);
    void deleteAll();
    void deleteByIds(List<Long> ids);

    List<ExecutionRun> findRunsByExecutionId(Long executionId);
    ExecutionRun saveRun(ExecutionRun run);
    int countNextRunNumber(Long executionId);
}
