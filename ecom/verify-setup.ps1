# GCP Storage Module - Setup Verification Script (PowerShell)

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "GCP Storage Module Setup Verification" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Check if gcp-credentials.json exists
if (Test-Path "src\main\resources\gcp-credentials.json") {
    Write-Host "✅ GCP credentials file found" -ForegroundColor Green
} else {
    Write-Host "❌ GCP credentials file NOT found" -ForegroundColor Red
    Write-Host "   Please add gcp-credentials.json to src\main\resources\" -ForegroundColor Yellow
}

# Check if application.properties has GCP config
$appProps = Get-Content "src\main\resources\application.properties" -Raw
if ($appProps -match "gcp.storage.project-id") {
    Write-Host "✅ GCP configuration found in application.properties" -ForegroundColor Green
} else {
    Write-Host "❌ GCP configuration NOT found in application.properties" -ForegroundColor Red
}

# Check if GcpStorageService exists
if (Test-Path "src\main\java\com\tcon\ecom\service\GcpStorageService.java") {
    Write-Host "✅ GcpStorageService found" -ForegroundColor Green
} else {
    Write-Host "❌ GcpStorageService NOT found" -ForegroundColor Red
}

# Check if StorageController exists
if (Test-Path "src\main\java\com\tcon\ecom\controller\StorageController.java") {
    Write-Host "✅ StorageController found" -ForegroundColor Green
} else {
    Write-Host "❌ StorageController NOT found" -ForegroundColor Red
}

# Check if pom.xml has GCP dependency
$pomXml = Get-Content "pom.xml" -Raw
if ($pomXml -match "google-cloud-storage") {
    Write-Host "✅ Google Cloud Storage dependency found in pom.xml" -ForegroundColor Green
} else {
    Write-Host "❌ Google Cloud Storage dependency NOT found in pom.xml" -ForegroundColor Red
}

# Check if build was successful
if (Test-Path "target\ecom-0.0.1-SNAPSHOT.jar") {
    Write-Host "✅ Application JAR file found (build successful)" -ForegroundColor Green
} else {
    Write-Host "⚠️  Application JAR file NOT found (run build first)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "1. If credentials file is missing, follow GCP_STORAGE_SETUP.md" -ForegroundColor White
Write-Host "2. Update application.properties with your GCP project ID" -ForegroundColor White
Write-Host "3. Run: .\mvnw.cmd spring-boot:run" -ForegroundColor White
Write-Host "4. Test endpoints using Postman or cURL" -ForegroundColor White
Write-Host ""

