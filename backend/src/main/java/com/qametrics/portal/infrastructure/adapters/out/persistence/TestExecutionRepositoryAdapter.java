package com.qametrics.portal.infrastructure.adapters.out.persistence;

import com.qametrics.portal.domain.model.*;
import com.qametrics.portal.domain.port.outbound.TestExecutionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TestExecutionRepositoryAdapter implements TestExecutionRepository {

    private final TestExecutionJpaRepository execRepo;
    private final TestExecutionRunJpaRepository runRepo;

    public TestExecutionRepositoryAdapter(TestExecutionJpaRepository execRepo,
                                          TestExecutionRunJpaRepository runRepo) {
        this.execRepo = execRepo;
        this.runRepo  = runRepo;
    }

    @Override
    public List<TestExecution> findAll(ProjectType projectType, String sprintOrPi, Integer year, Integer month) {
        return findAll(projectType, null, sprintOrPi, year, month);
    }

    @Override
    public List<TestExecution> findAll(ProjectType projectType, RequestType requestType, String sprintOrPi, Integer year, Integer month) {
        String pt = (projectType != null) ? projectType.name() : null;
        String rt = (requestType != null) ? requestType.name() : null;
        return execRepo.findByFilters(pt, rt, sprintOrPi, year, month)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<TestExecution> findById(Long id) {
        return execRepo.findById(id).map(this::toDomain);
    }

    @Override
    public TestExecution save(TestExecution exec) {
        return toDomain(execRepo.save(toEntity(exec)));
    }

    @Override
    public void deleteById(Long id) {
        execRepo.deleteById(id);
    }

    @Override
    public void deleteAll() {
        execRepo.deleteAll();
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        execRepo.deleteAllById(ids);
    }

    @Override
    public List<ExecutionRun> findRunsByExecutionId(Long executionId) {
        return runRepo.findByTestExecutionId(executionId)
                .stream().map(this::runToDomain).collect(Collectors.toList());
    }

    @Override
    public ExecutionRun saveRun(ExecutionRun run) {
        TestExecutionEntity parent = execRepo.findById(run.getTestExecutionId())
                .orElseThrow(() -> new IllegalArgumentException("Ejecución no encontrada"));
        TestExecutionRunEntity entity = runToEntity(run, parent);
        return runToDomain(runRepo.save(entity));
    }

    @Override
    public int countNextRunNumber(Long executionId) {
        return execRepo.countNextRunNumber(executionId);
    }

    // ── Mapeos ──────────────────────────────────────────────
    private TestExecution toDomain(TestExecutionEntity e) {
        TestExecution t = new TestExecution();
        t.setId(e.getId());
        t.setJiraId(e.getJiraId());
        t.setProjectType(ProjectType.valueOf(e.getProjectType()));
        if (e.getRequestType() != null) {
            try { t.setRequestType(RequestType.valueOf(e.getRequestType())); } catch (Exception ex) { t.setRequestType(RequestType.EVOLUTIVO); }
        }
        t.setAssignmentDate(e.getAssignmentDate());
        t.setDesignDate(e.getDesignDate());
        t.setDesignerAnalyst(e.getDesignerAnalyst());
        t.setCommitmentDate(e.getCommitmentDate());
        t.setQaDeliveryDate(e.getQaDeliveryDate());
        t.setClientDeliveryDate(e.getClientDeliveryDate());
        t.setSprintOrPi(e.getSprintOrPi());
        t.setDescription(e.getDescription());
        t.setTotalCases(e.getTotalCases());
        t.setSuccessfulCases(e.getSuccessfulCases());
        t.setFailedCases(e.getFailedCases());
        t.setBlockedCases(e.getBlockedCases());
        t.setCreatedAt(e.getCreatedAt());
        t.setCreatedBy(e.getCreatedBy());
        t.setLastModifiedBy(e.getLastModifiedBy());
        t.setUpdatedAt(e.getUpdatedAt());
        if (e.getRuns() != null) {
            t.setRuns(e.getRuns().stream().map(this::runToDomain).collect(Collectors.toList()));
        }
        return t;
    }

    private TestExecutionEntity toEntity(TestExecution t) {
        TestExecutionEntity e = new TestExecutionEntity();
        if (t.getId() != null) e.setId(t.getId());
        e.setJiraId(t.getJiraId());
        e.setProjectType(t.getProjectType().name());
        if (t.getRequestType() != null) e.setRequestType(t.getRequestType().name());
        e.setAssignmentDate(t.getAssignmentDate());
        e.setDesignDate(t.getDesignDate());
        e.setDesignerAnalyst(t.getDesignerAnalyst());
        e.setCommitmentDate(t.getCommitmentDate());
        e.setQaDeliveryDate(t.getQaDeliveryDate());
        e.setClientDeliveryDate(t.getClientDeliveryDate());
        e.setSprintOrPi(t.getSprintOrPi());
        e.setDescription(t.getDescription());
        e.setTotalCases(t.getTotalCases());
        e.setSuccessfulCases(t.getSuccessfulCases());
        e.setFailedCases(t.getFailedCases());
        e.setBlockedCases(t.getBlockedCases());
        if (t.getCreatedAt() != null) e.setCreatedAt(t.getCreatedAt());
        if (t.getCreatedBy() != null) e.setCreatedBy(t.getCreatedBy());
        if (t.getLastModifiedBy() != null) e.setLastModifiedBy(t.getLastModifiedBy());
        if (t.getUpdatedAt() != null) e.setUpdatedAt(t.getUpdatedAt());

        if (t.getRuns() != null && !t.getRuns().isEmpty()) {
            List<TestExecutionRunEntity> runEntities = t.getRuns().stream()
                    .map(r -> runToEntity(r, e))
                    .collect(Collectors.toList());
            e.setRuns(runEntities);
        }
        return e;
    }

    private ExecutionRun runToDomain(TestExecutionRunEntity r) {
        return new ExecutionRun(
                r.getId(),
                r.getTestExecution() != null ? r.getTestExecution().getId() : null,
                r.getRunNumber(),
                r.getExecutionDate(),
                r.getExecutedByAnalyst(),
                RunStatus.valueOf(r.getStatus()),
                r.getNotes(),
                r.getCasesExecuted(),
                r.getCasesPassed(),
                r.getCasesFailed(),
                r.getCasesBlocked()
        );
    }

    private TestExecutionRunEntity runToEntity(ExecutionRun run, TestExecutionEntity parent) {
        TestExecutionRunEntity e = new TestExecutionRunEntity();
        if (run.getId() != null) e.setId(run.getId());
        e.setTestExecution(parent);
        e.setRunNumber(run.getRunNumber());
        e.setExecutionDate(run.getExecutionDate());
        e.setExecutedByAnalyst(run.getExecutedByAnalyst());
        e.setStatus(run.getStatus().name());
        e.setNotes(run.getNotes());
        e.setCasesExecuted(run.getCasesExecuted());
        e.setCasesPassed(run.getCasesPassed());
        e.setCasesFailed(run.getCasesFailed());
        e.setCasesBlocked(run.getCasesBlocked());
        return e;
    }
}
