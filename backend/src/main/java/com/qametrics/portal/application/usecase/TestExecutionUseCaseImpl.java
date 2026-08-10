package com.qametrics.portal.application.usecase;

import com.qametrics.portal.domain.model.ExecutionRun;
import com.qametrics.portal.domain.model.ProjectType;
import com.qametrics.portal.domain.model.TestExecution;
import com.qametrics.portal.domain.port.inbound.TestExecutionUseCase;
import com.qametrics.portal.domain.port.outbound.TestExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Caso de uso de Ejecuciones de Prueba y Retests N-iterativos.
 */
@Service
@Transactional
public class TestExecutionUseCaseImpl implements TestExecutionUseCase {

    private final TestExecutionRepository repository;

    public TestExecutionUseCaseImpl(TestExecutionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestExecution> findAll(ProjectType projectType, String sprintOrPi, Integer year, Integer month) {
        return repository.findAll(projectType, sprintOrPi, year, month);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TestExecution> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public TestExecution create(TestExecution execution) {
        return repository.save(execution);
    }

    @Override
    public TestExecution update(Long id, TestExecution execution) {
        repository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Ejecución no encontrada con ID: " + id));
        execution.setId(id);
        return repository.save(execution);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        repository.deleteByIds(ids);
    }

    @Override
    public ExecutionRun addRun(Long executionId, ExecutionRun run) {
        repository.findById(executionId).orElseThrow(
                () -> new IllegalArgumentException("Ejecución no encontrada: " + executionId));

        // Auto-numerar el retest
        int nextRunNumber = repository.countNextRunNumber(executionId);
        run.setTestExecutionId(executionId);
        run.setRunNumber(nextRunNumber);
        return repository.saveRun(run);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecutionRun> findRunsByExecutionId(Long executionId) {
        return repository.findRunsByExecutionId(executionId);
    }
}
