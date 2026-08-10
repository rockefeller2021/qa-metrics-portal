package com.qametrics.portal.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ClientReturn {

    private Long id;
    private Long clientDeliveryMetricId;
    private ProjectType projectType = ProjectType.FABRICA;
    private int year = LocalDate.now().getYear();
    private int month = LocalDate.now().getMonthValue();
    private String ibl;
    private ReturnCategory category;
    private String rootCause;
    private int returnCount = 1;
    private boolean countedInQuality = false;
    private LocalDate returnDate;
    private LocalDateTime createdAt;
    private String createdBy;
    private String lastModifiedBy;
    private LocalDateTime updatedAt;

    public ClientReturn() {}

    public ClientReturn(Long id, Long clientDeliveryMetricId, String ibl, ReturnCategory category,
                        String rootCause, int returnCount, boolean countedInQuality, LocalDate returnDate) {
        this.id = id;
        this.clientDeliveryMetricId = clientDeliveryMetricId;
        this.ibl = ibl;
        this.category = category;
        this.rootCause = rootCause;
        this.returnCount = returnCount;
        this.countedInQuality = countedInQuality;
        this.returnDate = returnDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClientDeliveryMetricId() { return clientDeliveryMetricId; }
    public void setClientDeliveryMetricId(Long id) { this.clientDeliveryMetricId = id; }
    public ProjectType getProjectType() { return projectType; }
    public void setProjectType(ProjectType projectType) { this.projectType = projectType; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public String getIbl() { return ibl; }
    public void setIbl(String ibl) { this.ibl = ibl; }
    public ReturnCategory getCategory() { return category; }
    public void setCategory(ReturnCategory category) { this.category = category; }
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
