package com.qametrics.portal.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de dominio pura — Ejecución de Prueba / Requerimiento.
 * Contiene N iteraciones de retest (relación 1 a N).
 */
public class TestExecution {

    private Long id;
    private String jiraId;
    private ProjectType projectType;
    private RequestType requestType = RequestType.EVOLUTIVO;
    private LocalDate assignmentDate;
    private LocalDate designDate;
    private String designerAnalyst;
    private LocalDate commitmentDate;
    private LocalDate qaDeliveryDate;
    private LocalDate clientDeliveryDate;
    private String sprintOrPi;
    private String description;
    private int totalCases;
    private int successfulCases;
    private int failedCases;
    private int blockedCases;
    private LocalDateTime createdAt;
    private String createdBy;
    private String lastModifiedBy;
    private LocalDateTime updatedAt;
    private List<ExecutionRun> runs = new ArrayList<>();

    public TestExecution() {}

    public TestExecution(Long id, String jiraId, ProjectType projectType, LocalDate assignmentDate,
                         LocalDate designDate, String designerAnalyst, LocalDate commitmentDate,
                         LocalDate qaDeliveryDate, LocalDate clientDeliveryDate, String sprintOrPi,
                         String description, int totalCases, int successfulCases, int failedCases,
                         int blockedCases, LocalDateTime createdAt, List<ExecutionRun> runs) {
        this.id = id;
        this.jiraId = jiraId;
        this.projectType = projectType;
        this.assignmentDate = assignmentDate;
        this.designDate = designDate;
        this.designerAnalyst = designerAnalyst;
        this.commitmentDate = commitmentDate;
        this.qaDeliveryDate = qaDeliveryDate;
        this.clientDeliveryDate = clientDeliveryDate;
        this.sprintOrPi = sprintOrPi;
        this.description = description;
        this.totalCases = totalCases;
        this.successfulCases = successfulCases;
        this.failedCases = failedCases;
        this.blockedCases = blockedCases;
        this.createdAt = createdAt;
        this.runs = runs != null ? runs : new ArrayList<>();
    }

    // ── Lógica de negocio ────────────────────────────────────
    public int getTotalRuns() {
        return runs.size();
    }

    public long countSuccessfulRuns() {
        return runs.stream()
                .filter(r -> RunStatus.SUCCESSFUL.equals(r.getStatus()))
                .count();
    }

    public ExecutionRun getLatestRun() {
        return runs.stream()
                .max((a, b) -> Integer.compare(a.getRunNumber(), b.getRunNumber()))
                .orElse(null);
    }

    public boolean isCurrentlyPassing() {
        ExecutionRun latest = getLatestRun();
        return latest != null && RunStatus.SUCCESSFUL.equals(latest.getStatus());
    }

    public boolean isSlaBreached() {
        if (commitmentDate == null || qaDeliveryDate == null) return false;
        return qaDeliveryDate.isAfter(commitmentDate);
    }

    // ── Getters & Setters ────────────────────────────────────
    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }
    public String getJiraId()              { return jiraId; }
    public void setJiraId(String jiraId)   { this.jiraId = jiraId; }
    public ProjectType getProjectType()                 { return projectType; }
    public void setProjectType(ProjectType projectType) { this.projectType = projectType; }
    public RequestType getRequestType()                 { return requestType; }
    public void setRequestType(RequestType requestType) { this.requestType = requestType; }
    public LocalDate getAssignmentDate()                   { return assignmentDate; }
    public void setAssignmentDate(LocalDate assignmentDate){ this.assignmentDate = assignmentDate; }
    public LocalDate getDesignDate()              { return designDate; }
    public void setDesignDate(LocalDate designDate){ this.designDate = designDate; }
    public String getDesignerAnalyst()                   { return designerAnalyst; }
    public void setDesignerAnalyst(String designerAnalyst){ this.designerAnalyst = designerAnalyst; }
    public LocalDate getCommitmentDate()               { return commitmentDate; }
    public void setCommitmentDate(LocalDate commitmentDate){ this.commitmentDate = commitmentDate; }
    public LocalDate getQaDeliveryDate()               { return qaDeliveryDate; }
    public void setQaDeliveryDate(LocalDate qaDeliveryDate){ this.qaDeliveryDate = qaDeliveryDate; }
    public LocalDate getClientDeliveryDate()                   { return clientDeliveryDate; }
    public void setClientDeliveryDate(LocalDate clientDeliveryDate){ this.clientDeliveryDate = clientDeliveryDate; }
    public String getSprintOrPi()              { return sprintOrPi; }
    public void setSprintOrPi(String sprintOrPi){ this.sprintOrPi = sprintOrPi; }
    public String getDescription()               { return description; }
    public void setDescription(String description){ this.description = description; }
    public int getTotalCases()            { return totalCases; }
    public void setTotalCases(int total)  { this.totalCases = total; }
    public int getSuccessfulCases()       { return successfulCases; }
    public void setSuccessfulCases(int s) { this.successfulCases = s; }
    public int getFailedCases()           { return failedCases; }
    public void setFailedCases(int f)     { this.failedCases = f; }
    public int getBlockedCases()          { return blockedCases; }
    public void setBlockedCases(int b)    { this.blockedCases = b; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<ExecutionRun> getRuns()          { return runs; }
    public void setRuns(List<ExecutionRun> runs) { this.runs = runs; }
}
