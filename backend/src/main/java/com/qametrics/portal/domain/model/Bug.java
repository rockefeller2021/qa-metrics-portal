package com.qametrics.portal.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad de dominio pura — Bug / Incidencia de QA.
 * Sin dependencias de Spring ni JPA.
 */
public class Bug {

    private Long id;
    private String bugJiraId;
    private String requirementId;
    private ProjectType projectType;
    private String sprintOrPi;
    private BugStatus status;
    private DefectType defectType;
    private String description;
    private boolean reinjectionFlag;
    private LocalDate reportedDate;
    private LocalDate resolvedDate;
    private String reportedBy;
    private String developerName;
    private LocalDateTime createdAt;
    private String createdBy;
    private String lastModifiedBy;
    private LocalDateTime updatedAt;

    public Bug() {}

    // ── Lógica de negocio ────────────────────────────────────
    public boolean isOpen()     { return BugStatus.OPEN.equals(this.status); }
    public boolean isResolved() { return BugStatus.RESOLVED.equals(this.status) || BugStatus.CLOSED.equals(this.status); }
    public boolean isReinjection() { return this.reinjectionFlag; }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public int getDaysOpen() {
        if (reportedDate == null) return 0;
        LocalDate end = (resolvedDate != null) ? resolvedDate : LocalDate.now();
        if (reportedDate.isAfter(end)) return 0;
        return (int) reportedDate.datesUntil(end).count();
    }

    // ── Getters & Setters ────────────────────────────────────
    public Long getId()                { return id; }
    public void setId(Long id)         { this.id = id; }

    public String getBugJiraId()               { return bugJiraId; }
    public void setBugJiraId(String bugJiraId) { this.bugJiraId = bugJiraId; }

    public String getRequirementId()                 { return requirementId; }
    public void setRequirementId(String requirementId){ this.requirementId = requirementId; }

    public ProjectType getProjectType()               { return projectType; }
    public void setProjectType(ProjectType projectType){ this.projectType = projectType; }

    public String getSprintOrPi()              { return sprintOrPi; }
    public void setSprintOrPi(String sprintOrPi){ this.sprintOrPi = sprintOrPi; }

    public BugStatus getStatus()           { return status; }
    public void setStatus(BugStatus status){ this.status = status; }

    public DefectType getDefectType()            { return defectType; }
    public void setDefectType(DefectType defectType){ this.defectType = defectType; }

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

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
