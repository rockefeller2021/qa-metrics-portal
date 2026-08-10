package com.qametrics.portal.infrastructure.adapters.out.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "test_execution_runs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"test_execution_id", "run_number"}))
public class TestExecutionRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_execution_id", nullable = false)
    private TestExecutionEntity testExecution;

    @Column(name = "run_number", nullable = false)
    private int runNumber;

    @Column(name = "execution_date", nullable = false)
    private LocalDate executionDate;

    @Column(name = "executed_by_analyst", nullable = false, length = 100)
    private String executedByAnalyst;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "cases_executed", nullable = false)
    private int casesExecuted = 0;

    @Column(name = "cases_passed", nullable = false)
    private int casesPassed = 0;

    @Column(name = "cases_failed", nullable = false)
    private int casesFailed = 0;

    @Column(name = "cases_blocked", nullable = false)
    private int casesBlocked = 0;

    // Getters & Setters
    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }
    public TestExecutionEntity getTestExecution()              { return testExecution; }
    public void setTestExecution(TestExecutionEntity te)      { this.testExecution = te; }
    public int getRunNumber()              { return runNumber; }
    public void setRunNumber(int runNumber){ this.runNumber = runNumber; }
    public LocalDate getExecutionDate()                { return executionDate; }
    public void setExecutionDate(LocalDate executionDate){ this.executionDate = executionDate; }
    public String getExecutedByAnalyst()                   { return executedByAnalyst; }
    public void setExecutedByAnalyst(String executedByAnalyst){ this.executedByAnalyst = executedByAnalyst; }
    public String getStatus()          { return status; }
    public void setStatus(String status){ this.status = status; }
    public String getNotes()           { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public int getCasesExecuted()                       { return casesExecuted; }
    public void setCasesExecuted(int casesExecuted)     { this.casesExecuted = casesExecuted; }
    public int getCasesPassed()                         { return casesPassed; }
    public void setCasesPassed(int casesPassed)         { this.casesPassed = casesPassed; }
    public int getCasesFailed()                         { return casesFailed; }
    public void setCasesFailed(int casesFailed)         { this.casesFailed = casesFailed; }
    public int getCasesBlocked()                        { return casesBlocked; }
    public void setCasesBlocked(int casesBlocked)       { this.casesBlocked = casesBlocked; }
}
