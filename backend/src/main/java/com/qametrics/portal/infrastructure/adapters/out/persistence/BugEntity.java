package com.qametrics.portal.infrastructure.adapters.out.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bugs")
public class BugEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bug_jira_id",  nullable = false, unique = true, length = 50)
    private String bugJiraId;

    @Column(name = "requirement_id", nullable = false, length = 50)
    private String requirementId;

    @Column(name = "project_type", nullable = false, length = 20)
    private String projectType;

    @Column(name = "request_type", nullable = false, length = 30)
    private String requestType = "EVOLUTIVO";

    @Column(name = "sprint_or_pi", nullable = false, length = 50)
    private String sprintOrPi;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "defect_type", nullable = false, length = 30)
    private String defectType;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "reinjection_flag", nullable = false)
    private boolean reinjectionFlag = false;

    @Column(name = "reported_date", nullable = false)
    private LocalDate reportedDate;

    @Column(name = "resolved_date")
    private LocalDate resolvedDate;

    @Column(name = "reported_by", length = 100)
    private String reportedBy;

    @Column(name = "developer_name", length = 100)
    private String developerName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "last_modified_by", length = 100)
    private String lastModifiedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
    public String getBugJiraId()               { return bugJiraId; }
    public void setBugJiraId(String bugJiraId) { this.bugJiraId = bugJiraId; }
    public String getRequirementId()                 { return requirementId; }
    public void setRequirementId(String requirementId){ this.requirementId = requirementId; }
    public String getProjectType()                 { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
    public String getRequestType()                 { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public String getSprintOrPi()              { return sprintOrPi; }
    public void setSprintOrPi(String sprintOrPi){ this.sprintOrPi = sprintOrPi; }
    public String getStatus()          { return status; }
    public void setStatus(String status){ this.status = status; }
    public String getDefectType()            { return defectType; }
    public void setDefectType(String defectType){ this.defectType = defectType; }
    public String getDescription()               { return description; }
    public void setDescription(String description){ this.description = description; }
    public boolean isReinjectionFlag()                 { return reinjectionFlag; }
    public void setReinjectionFlag(boolean reinjectionFlag){ this.reinjectionFlag = reinjectionFlag; }
    public LocalDate getReportedDate()               { return reportedDate; }
    public void setReportedDate(LocalDate reportedDate){ this.reportedDate = reportedDate; }
    public LocalDate getResolvedDate()               { return resolvedDate; }
    public void setResolvedDate(LocalDate resolvedDate){ this.resolvedDate = resolvedDate; }
    public String getReportedBy()              { return reportedBy; }
    public void setReportedBy(String reportedBy){ this.reportedBy = reportedBy; }
    public String getDeveloperName()                 { return developerName; }
    public void setDeveloperName(String developerName){ this.developerName = developerName; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
}
