package com.qametrics.portal.infrastructure.adapters.out.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "client_returns")
public class ClientReturnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_delivery_metric_id")
    private ClientDeliveryMetricEntity clientDeliveryMetric;

    @Column(name = "project_type", nullable = false, length = 30)
    private String projectType = "FABRICA";

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "ibl", nullable = false, length = 50)
    private String ibl;

    @Column(name = "category", nullable = false, length = 30)
    private String category;

    @Column(name = "root_cause", nullable = false, columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "return_count", nullable = false)
    private int returnCount = 1;

    @Column(name = "counted_in_quality", nullable = false)
    private boolean countedInQuality = false;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ClientDeliveryMetricEntity getClientDeliveryMetric() { return clientDeliveryMetric; }
    public void setClientDeliveryMetric(ClientDeliveryMetricEntity cdm) { this.clientDeliveryMetric = cdm; }
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public String getIbl() { return ibl; }
    public void setIbl(String ibl) { this.ibl = ibl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }
    public int getReturnCount() { return returnCount; }
    public void setReturnCount(int returnCount) { this.returnCount = returnCount; }
    public boolean isCountedInQuality() { return countedInQuality; }
    public void setCountedInQuality(boolean countedInQuality) { this.countedInQuality = countedInQuality; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
