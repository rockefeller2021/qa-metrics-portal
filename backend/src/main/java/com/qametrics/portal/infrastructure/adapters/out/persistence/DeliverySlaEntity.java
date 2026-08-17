package com.qametrics.portal.infrastructure.adapters.out.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_slas")
public class DeliverySlaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jira_id", nullable = false, unique = true, length = 50)
    private String jiraId;

    @Column(name = "project_type", nullable = false, length = 20)
    private String projectType;

    @Column(name = "request_type", nullable = false, length = 30)
    private String requestType = "EVOLUTIVO";

    @Column(name = "sprint_or_pi", nullable = false, length = 50)
    private String sprintOrPi;

    @Column(name = "designer_analyst", nullable = false, length = 100)
    private String designerAnalyst;

    @Column(name = "estimated_delivery_date", nullable = false)
    private LocalDate estimatedDeliveryDate;

    @Column(name = "estimated_qa_date")
    private LocalDate estimatedQaDate;

    @Column(name = "real_qa_date")
    private LocalDate realQaDate;

    @Column(name = "real_client_delivery_date")
    private LocalDate realClientDeliveryDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "delay_days", nullable = false)
    private int delayDays = 0;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

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
    public Long getId()                               { return id; }
    public void setId(Long id)                        { this.id = id; }
    public String getJiraId()                          { return jiraId; }
    public void setJiraId(String jiraId)              { this.jiraId = jiraId; }
    public String getProjectType()                 { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
    public String getRequestType()                 { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    public String getSprintOrPi()              { return sprintOrPi; }
    public void setSprintOrPi(String sprintOrPi){ this.sprintOrPi = sprintOrPi; }
    public String getDesignerAnalyst()                { return designerAnalyst; }
    public void setDesignerAnalyst(String designerAnalyst){ this.designerAnalyst = designerAnalyst; }
    public LocalDate getEstimatedDeliveryDate()       { return estimatedDeliveryDate; }
    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate){ this.estimatedDeliveryDate = estimatedDeliveryDate; }
    public LocalDate getEstimatedQaDate()             { return estimatedQaDate; }
    public void setEstimatedQaDate(LocalDate estimatedQaDate){ this.estimatedQaDate = estimatedQaDate; }
    public LocalDate getRealQaDate()                  { return realQaDate; }
    public void setRealQaDate(LocalDate realQaDate)   { this.realQaDate = realQaDate; }
    public LocalDate getRealClientDeliveryDate()      { return realClientDeliveryDate; }
    public void setRealClientDeliveryDate(LocalDate realClientDeliveryDate){ this.realClientDeliveryDate = realClientDeliveryDate; }
    public String getStatus()                      { return status; }
    public void setStatus(String status)          { this.status = status; }
    public int getDelayDays()                         { return delayDays; }
    public void setDelayDays(int delayDays)           { this.delayDays = delayDays; }
    public String getNotes()                          { return notes; }
    public void setNotes(String notes)                { this.notes = notes; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
}
