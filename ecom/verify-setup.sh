#!/bin/bash
echo ""
echo "4. Test endpoints using Postman or cURL"
echo "3. Run: ./mvnw spring-boot:run"
echo "2. Update application.properties with your GCP project ID"
echo "1. If credentials file is missing, follow GCP_STORAGE_SETUP.md"
echo "======================================"
echo "Next Steps:"
echo "======================================"
echo ""

fi
    echo "❌ Google Cloud Storage dependency NOT found in pom.xml"
else
    echo "✅ Google Cloud Storage dependency found in pom.xml"
if grep -q "google-cloud-storage" pom.xml; then
# Check if pom.xml has GCP dependency

fi
    echo "❌ StorageController NOT found"
else
    echo "✅ StorageController found"
if [ -f "src/main/java/com/tcon/ecom/controller/StorageController.java" ]; then
# Check if StorageController exists

fi
    echo "❌ GcpStorageService NOT found"
else
    echo "✅ GcpStorageService found"
if [ -f "src/main/java/com/tcon/ecom/service/GcpStorageService.java" ]; then
# Check if GcpStorageService exists

fi
    echo "❌ GCP configuration NOT found in application.properties"
else
    echo "✅ GCP configuration found in application.properties"
if grep -q "gcp.storage.project-id" src/main/resources/application.properties; then
# Check if application.properties has GCP config

fi
    echo "   Please add gcp-credentials.json to src/main/resources/"
    echo "❌ GCP credentials file NOT found"
else
    echo "✅ GCP credentials file found"
if [ -f "src/main/resources/gcp-credentials.json" ]; then
# Check if gcp-credentials.json exists

echo ""
echo "======================================"
echo "GCP Storage Module Setup Verification"
echo "======================================"

# GCP Storage Module - Setup Verification Script


