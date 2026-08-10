package com.qametrics.portal.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entidad de Dominio — Seguimiento de Hitos de Entrega y Trazabilidad SLA (RF04).
 */
public class DeliverySla {

    private Long id;
    private String jiraId;
    private ProjectType projectType;
    private String sprintOrPi;
    private String designerAnalyst;
    private LocalDate estimatedDeliveryDate; // Fecha estimada de entrega cliente
    private LocalDate estimatedQaDate;       // Fecha pruebas QA estimada
    private LocalDate realQaDate;            // Fecha real QA
    private LocalDate realClientDeliveryDate;// Fecha real entrega cliente
    private SlaStatus status;
    private int delayDays;
    private String notes;
    private LocalDateTime createdAt;
    private String createdBy;
    private String lastModifiedBy;
    private LocalDateTime updatedAt;

    public DeliverySla() {}

    // ── Lógica de negocio de cálculo de SLA ───────────────────
    public void recalculateSlaStatus() {
        LocalDate checkDate = (realClientDeliveryDate != null) ? realClientDeliveryDate : LocalDate.now();

        if (estimatedDeliveryDate == null) {
            this.status = SlaStatus.PENDING;
            this.delayDays = 0;
            return;
        }

        if (realClientDeliveryDate == null) {
            // Aún no entregado al cliente
            if (LocalDate.now().isAfter(estimatedDeliveryDate)) {
                this.status = SlaStatus.DELAYED;
                this.delayDays = (int) ChronoUnit.DAYS.between(estimatedDeliveryDate, LocalDate.now());
            } else {
                this.status = SlaStatus.PENDING;
                this.delayDays = 0;
            }
        } else {
            // Ya entregado
            if (realClientDeliveryDate.isAfter(estimatedDeliveryDate)) {
                this.status = SlaStatus.DELAYED;
                this.delayDays = (int) ChronoUnit.DAYS.between(estimatedDeliveryDate, realClientDeliveryDate);
            } else {
                this.status = SlaStatus.ON_TIME;
                this.delayDays = 0;
            }
        }
    }

    // ── Getters & Setters ────────────────────────────────────
    public Long getId()                               { return id; }
    public void setId(Long id)                        { this.id = id; }

    public String getJiraId()                          { return jiraId; }
    public void setJiraId(String jiraId)              { this.jiraId = jiraId; }

    public ProjectType getProjectType()              { return projectType; }
    public void setProjectType(ProjectType projectType){ this.projectType = projectType; }

    public String getSprintOrPi()                     { return sprintOrPi; }
    public void setSprintOrPi(String sprintOrPi)      { this.sprintOrPi = sprintOrPi; }

    public String getDesignerAnalyst()                { return designerAnalyst; }
    public void setDesignerAnalyst(String designerAnalyst){ this.designerAnalyst = designerAnalyst; }

    public LocalDate getEstimatedDeliveryDate()       { return estimatedDeliveryDate; }
    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate){
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public LocalDate getEstimatedQaDate()             { return estimatedQaDate; }
    public void setEstimatedQaDate(LocalDate estimatedQaDate){
        this.estimatedQaDate = estimatedQaDate;
    }

    public LocalDate getRealQaDate()                  { return realQaDate; }
    public void setRealQaDate(LocalDate realQaDate)   { this.realQaDate = realQaDate; }

    public LocalDate getRealClientDeliveryDate()      { return realClientDeliveryDate; }
    public void setRealClientDeliveryDate(LocalDate realClientDeliveryDate){
        this.realClientDeliveryDate = realClientDeliveryDate;
    }

    public SlaStatus getStatus()                      { return status; }
    public void setStatus(SlaStatus status)          { this.status = status; }

    public int getDelayDays()                         { return delayDays; }
    public void setDelayDays(int delayDays)           { this.delayDays = delayDays; }

    public String getNotes()                          { return notes; }
    public void setNotes(String notes)                { this.notes = notes; }

    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
