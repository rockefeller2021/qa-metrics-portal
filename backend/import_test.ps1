$csvContent = @"
JIRA_ID,LINEA,ANALISTA,DISEÑADOS,OK,FAIL,BLOCK,SPRINT,FECHA
JIRA-JUNE-100,FABRICA,Analista QA Junio,25,23,2,0,Sprint 15,2026-06-15
JIRA-JUNE-101,MINOR_DEMAND,Analista QA Junio,30,29,1,0,Sprint 15,2026-06-20
JIRA-JULY-100,FABRICA,Analista QA Julio,40,38,2,0,Sprint 16,2026-07-05
JIRA-JULY-101,MINOR_DEMAND,Analista QA Julio,15,14,1,0,Sprint 16,2026-07-18
"@

$csvPath = Join-Path $PSScriptRoot "test_june_july.csv"
Set-Content -Path $csvPath -Value $csvContent -Encoding UTF8

$loginBody = @{ username = 'admin'; password = 'admin123' } | ConvertTo-Json
$loginRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/auth/login' -Method Post -Body $loginBody -ContentType 'application/json'
$headers = @{ Authorization = "Bearer $($loginRes.token)" }

Write-Host "Uploading CSV with June and July dates to /api/v1/import/executions..."

$boundary = [System.Guid]::NewGuid().ToString()
$fileBytes = [System.IO.File]::ReadAllBytes($csvPath)
$fileName = [System.IO.Path]::GetFileName($csvPath)

$LF = "`r`n"
$bodyLines = (
    "--$boundary",
    "Content-Disposition: form-data; name=`"file`"; filename=`"$fileName`"",
    "Content-Type: text/csv",
    "",
    [System.Text.Encoding]::UTF8.GetString($fileBytes),
    "--$boundary--"
) -join $LF

$uploadRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/import/executions' -Method Post -ContentType "multipart/form-data; boundary=$boundary" -Headers $headers -Body $bodyLines
Write-Host "Upload result:" ($uploadRes | ConvertTo-Json)

Write-Host "`n=== QUERYING EXECUTIONS FOR JUNE (month=6) ==="
$juneExecs = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/executions?month=6&year=2026' -Method Get -Headers $headers
$juneExecs | Select-Object id, jiraId, projectType, assignmentDate, designDate, createdAt | Format-Table -AutoSize

Write-Host "`n=== QUERYING EXECUTIONS FOR JULY (month=7) ==="
$julyExecs = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/executions?month=7&year=2026' -Method Get -Headers $headers
$julyExecs | Select-Object id, jiraId, projectType, assignmentDate, designDate, createdAt | Format-Table -AutoSize
