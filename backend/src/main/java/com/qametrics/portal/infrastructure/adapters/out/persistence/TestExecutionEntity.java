package com.qametrics.portal.infrastructure.adapters.out.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_executions")
public class TestExecutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jira_id", nullable = false, length = 50)
    private String jiraId;

    @Column(name = "project_type", nullable = false, length = 20)
    private String projectType;

    @Column(name = "assignment_date", nullable = false)
    private LocalDate assignmentDate;

    @Column(name = "design_date")
    private LocalDate designDate;

    @Column(name = "designer_analyst", nullable = false, length = 100)
    private String designerAnalyst;

    @Column(name = "commitment_date")
    private LocalDate commitmentDate;

    @Column(name = "qa_delivery_date")
    private LocalDate qaDeliveryDate;

    @Column(name = "client_delivery_date")
    private LocalDate clientDeliveryDate;

    @Column(name = "sprint_or_pi", length = 50)
    private String sprintOrPi;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_cases", nullable = false)
    private int totalCases = 0;

    @Column(name = "successful_cases", nullable = false)
    private int successfulCases = 0;

    @Column(name = "failed_cases", nullable = false)
    private int failedCases = 0;

    @Column(name = "blocked_cases", nullable = false)
    private int blockedCases = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "last_modified_by", length = 100)
    private String lastModifiedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "testExecution", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TestExecutionRunEntity> runs = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        String user = getCurrentUser();
        if (user != null) {
            this.createdBy = user;
            this.lastModifiedBy = user;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        String user = getCurrentUser();
        if (user != null) {
            this.lastModifiedBy = user;
        }
    }

    private String getCurrentUser() {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception e) {
            // Ignorar
        }
        return null;
    }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Getters & Setters
    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }
    public String getJiraId()              { return jiraId; }
    public void setJiraId(String jiraId)   { this.jiraId = jiraId; }
    public String getProjectType()                 { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
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
    public List<TestExecutionRunEntity> getRuns()          { return runs; }
    public void setRuns(List<TestExecutionRunEntity> runs) { this.runs = runs; }
}
