package com.qametrics.portal.infrastructure.adapters.out.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client_delivery_metrics")
public class ClientDeliveryMetricEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_type", nullable = false, length = 20)
    private String projectType;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "sprint_or_period", length = 50)
    private String sprintOrPeriod;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "evolutivos_count", nullable = false)
    private int evolutivosCount = 0;

    @Column(name = "soportes_count", nullable = false)
    private int soportesCount = 0;

    @Column(name = "standard_change_count", nullable = false)
    private int standardChangeCount = 0;

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

    @OneToMany(mappedBy = "clientDeliveryMetric", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ClientReturnEntity> returns = new ArrayList<>();

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public String getSprintOrPeriod() { return sprintOrPeriod; }
    public void setSprintOrPeriod(String sprintOrPeriod) { this.sprintOrPeriod = sprintOrPeriod; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }
    public int getEvolutivosCount() { return evolutivosCount; }
    public void setEvolutivosCount(int evolutivosCount) { this.evolutivosCount = evolutivosCount; }
    public int getSoportesCount() { return soportesCount; }
    public void setSoportesCount(int soportesCount) { this.soportesCount = soportesCount; }
    public int getStandardChangeCount() { return standardChangeCount; }
    public void setStandardChangeCount(int standardChangeCount) { this.standardChangeCount = standardChangeCount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<ClientReturnEntity> getReturns() { return returns; }
    public void setReturns(List<ClientReturnEntity> returns) { this.returns = returns; }
}
