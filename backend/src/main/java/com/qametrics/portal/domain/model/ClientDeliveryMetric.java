package com.qametrics.portal.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClientDeliveryMetric {

    private Long id;
    private ProjectType projectType;
    private int year;
    private int month;
    private String sprintOrPeriod;
    private LocalDate deliveryDate;
    private int evolutivosCount;
    private int soportesCount;
    private int standardChangeCount;
    private String notes;
    private LocalDateTime createdAt;
    private String createdBy;
    private String lastModifiedBy;
    private LocalDateTime updatedAt;
    private List<ClientReturn> returns = new ArrayList<>();

    public ClientDeliveryMetric() {}

    public ClientDeliveryMetric(Long id, ProjectType projectType, int year, int month, String sprintOrPeriod,
                                LocalDate deliveryDate, int evolutivosCount, int soportesCount,
                                int standardChangeCount, String notes) {
        this.id = id;
        this.projectType = projectType;
        this.year = year;
        this.month = month;
        this.sprintOrPeriod = sprintOrPeriod;
        this.deliveryDate = deliveryDate;
        this.evolutivosCount = evolutivosCount;
        this.soportesCount = soportesCount;
        this.standardChangeCount = standardChangeCount;
        this.notes = notes;
    }

    public int getTotalDeliveries() {
        return evolutivosCount + soportesCount + standardChangeCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ProjectType getProjectType() { return projectType; }
    public void setProjectType(ProjectType projectType) { this.projectType = projectType; }
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
    public List<ClientReturn> getReturns() { return returns; }
    public void setReturns(List<ClientReturn> returns) { this.returns = returns != null ? returns : new ArrayList<>(); }
}
