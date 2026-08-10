$loginBody = @{
    username = 'admin'
    password = 'admin123'
} | ConvertTo-Json

$loginRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/auth/login' -Method Post -Body $loginBody -ContentType 'application/json'
$headers = @{ Authorization = "Bearer $($loginRes.token)" }

Write-Host "=== TEST EXECUTIONS IN DATABASE ==="
$execs = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/executions' -Method Get -Headers $headers
$execs | Select-Object id, jiraId, projectType, assignmentDate, designDate, createdAt | Format-Table -AutoSize

Write-Host "=== TEST EXECUTIONS FOR JUNE (month=6) ==="
$juneExecs = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/executions?month=6' -Method Get -Headers $headers
$juneExecs | Select-Object id, jiraId, projectType, assignmentDate, designDate, createdAt | Format-Table -AutoSize

Write-Host "=== BUGS IN DATABASE ==="
$bugs = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/bugs' -Method Get -Headers $headers
$bugs | Select-Object id, bugJiraId, requirementId, reportedDate, createdAt | Format-Table -AutoSize

Write-Host "=== BUGS FOR JUNE (month=6) ==="
$juneBugs = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/bugs?month=6' -Method Get -Headers $headers
$juneBugs | Select-Object id, bugJiraId, requirementId, reportedDate, createdAt | Format-Table -AutoSize
