package com.qametrics.portal.domain.model;

import java.time.LocalDate;

/**
 * Entidad de dominio pura — Iteración/Retest de una ejecución.
 * Sin dependencias externas.
 */
public class ExecutionRun {

    private Long id;
    private Long testExecutionId;
    private int runNumber;
    private LocalDate executionDate;
    private String executedByAnalyst;
    private RunStatus status;
    private String notes;
    private int casesExecuted;
    private int casesPassed;
    private int casesFailed;
    private int casesBlocked;

    public ExecutionRun() {}

    public ExecutionRun(Long id, Long testExecutionId, int runNumber, LocalDate executionDate,
                        String executedByAnalyst, RunStatus status, String notes,
                        int casesExecuted, int casesPassed, int casesFailed, int casesBlocked) {
        this.id = id;
        this.testExecutionId = testExecutionId;
        this.runNumber = runNumber;
        this.executionDate = executionDate;
        this.executedByAnalyst = executedByAnalyst;
        this.status = status;
        this.notes = notes;
        this.casesExecuted = casesExecuted;
        this.casesPassed = casesPassed;
        this.casesFailed = casesFailed;
        this.casesBlocked = casesBlocked;
    }

    public ExecutionRun(Long id, Long testExecutionId, int runNumber, LocalDate executionDate,
                        String executedByAnalyst, RunStatus status, String notes) {
        this(id, testExecutionId, runNumber, executionDate, executedByAnalyst, status, notes, 0, 0, 0, 0);
    }

    public boolean isSuccessful() { return RunStatus.SUCCESSFUL.equals(this.status); }
    public boolean isFailed()     { return RunStatus.FAILED.equals(this.status); }
    public boolean isRetest()     { return RunStatus.RETEST.equals(this.status); }
    public boolean isInitialRun() { return this.runNumber == 1; }

    // Getters & Setters
    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }

    public Long getTestExecutionId()                   { return testExecutionId; }
    public void setTestExecutionId(Long testExecutionId){ this.testExecutionId = testExecutionId; }

    public int getRunNumber()              { return runNumber; }
    public void setRunNumber(int runNumber){ this.runNumber = runNumber; }

    public LocalDate getExecutionDate()                { return executionDate; }
    public void setExecutionDate(LocalDate executionDate){ this.executionDate = executionDate; }

    public String getExecutedByAnalyst()                   { return executedByAnalyst; }
    public void setExecutedByAnalyst(String executedByAnalyst){ this.executedByAnalyst = executedByAnalyst; }

    public RunStatus getStatus()           { return status; }
    public void setStatus(RunStatus status){ this.status = status; }

    public String getNotes()                            { return notes; }
    public void setNotes(String notes)                  { this.notes = notes; }

    public int getCasesExecuted()                       { return casesExecuted; }
    public void setCasesExecuted(int casesExecuted)     { this.casesExecuted = casesExecuted; }

    public int getCasesPassed()                         { return casesPassed; }
    public void setCasesPassed(int casesPassed)         { this.casesPassed = casesPassed; }

    public int getCasesFailed()                         { return casesFailed; }
    public void setCasesFailed(int casesFailed)         { this.casesFailed = casesFailed; }

    public int getCasesBlocked()                        { return casesBlocked; }
    public void setCasesBlocked(int casesBlocked)       { this.casesBlocked = casesBlocked; }
}
