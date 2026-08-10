package com.qametrics.portal.domain.port.inbound;

import com.qametrics.portal.domain.model.ProjectType;
import com.qametrics.portal.domain.model.TestExecution;
import com.qametrics.portal.domain.model.ExecutionRun;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de entrada para la gestión de Ejecuciones de Prueba y Retests.
 */
public interface TestExecutionUseCase {

    List<TestExecution> findAll(ProjectType projectType, String sprintOrPi, Integer year, Integer month);

    Optional<TestExecution> findById(Long id);

    TestExecution create(TestExecution execution);

    TestExecution update(Long id, TestExecution execution);

    void delete(Long id);

    /** Elimina todas las ejecuciones — solo ADMIN */
    void deleteAll();

    /** Elimina ejecuciones por lista de IDs — solo ADMIN */
    void deleteByIds(List<Long> ids);

    /** Agrega una nueva iteración de retest a una ejecución existente */
    ExecutionRun addRun(Long executionId, ExecutionRun run);

    List<ExecutionRun> findRunsByExecutionId(Long executionId);
}
