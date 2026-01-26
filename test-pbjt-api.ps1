# Test PBJT Assessment API Endpoints
Write-Host "=== TESTING PBJT ASSESSMENT API ===" -ForegroundColor Green
Write-Host ""

# Wait for app to fully start
Start-Sleep -Seconds 5

# Test 1: Health Check
Write-Host "1. Testing Health Check..." -ForegroundColor Yellow
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/pbjt-assessments/health" -Method Get
Write-Host "Response:" -ForegroundColor Cyan
$response | ConvertTo-Json
Write-Host ""

# Test 2: Get All Assessments
Write-Host "2. Testing Get All Assessments..." -ForegroundColor Yellow
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/pbjt-assessments?page=0&size=10" -Method Get
Write-Host "Response:" -ForegroundColor Cyan
Write-Host "Total Elements: $($response.pagination.totalElements)"
Write-Host "Data Count: $($response.data.Count)"
Write-Host ""

# Test 3: Get Assessment by ID
Write-Host "3. Testing Get Assessment by ID (ID=1)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/pbjt-assessments/1" -Method Get
    Write-Host "Response:" -ForegroundColor Cyan
    Write-Host "Business Name: $($response.data.businessName)"
    Write-Host "Annual PBJT: Rp $($response.data.annualPbjt)"
    Write-Host "Confidence Level: $($response.data.confidence.level)"
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 4: Get Count
Write-Host "4. Testing Get Count..." -ForegroundColor Yellow
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/pbjt-assessments/count" -Method Get
Write-Host "Response:" -ForegroundColor Cyan
$response | ConvertTo-Json
Write-Host ""

Write-Host "=== ALL TESTS COMPLETED ===" -ForegroundColor Green
