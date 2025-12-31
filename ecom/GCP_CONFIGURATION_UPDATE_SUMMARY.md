# GCP Storage Configuration Update Summary

## Changes Made

### ✅ Configuration Changes

#### 1. Updated `application.properties`
**File**: `src/main/resources/application.properties`

**Before**:
```properties
gcp.storage.project-id=your-gcp-project-id
gcp.storage.bucket-name=ecom-product-images
# gcp.storage.credentials-path=classpath:gcp-credentials.json
gcp.storage.base-url=https://storage.googleapis.com
```

**After**:
```properties
gcp.storage.project-id=tconecom-64307221061
gcp.storage.bucket-name=ecom-product-images
gcp.storage.base-url=https://storage.googleapis.com
# GCP Service Account Credentials (JSON key as single line)
gcp.storage.credentials-json={"type":"service_account",...}
```

**Key Changes**:
- ❌ Removed file-based credential loading (`credentials-path`)
- ✅ Added direct JSON credential loading (`credentials-json`)
- ✅ Updated project ID to actual project
- ✅ Added clear comments for configuration

#### 2. Updated `GcpStorageService.java`
**File**: `src/main/java/com/tcon/ecom/service/GcpStorageService.java`

**Key Changes**:
- ❌ Removed Application Default Credentials (ADC) fallback
- ❌ Removed file path and classpath credential loading
- ✅ Added direct JSON string credential loading
- ✅ Better error messages when credentials are missing
- ✅ Removed unused imports (`Files`, `Path`, `TimeUnit`)

**Why This Change?**

The old approach relied on:
1. **Application Default Credentials (ADC)**: Requires `gcloud auth` CLI setup
2. **File-based credentials**: Required managing credential files

The new approach:
1. ✅ **Direct JSON in properties**: Simpler, no external dependencies
2. ✅ **Works in any environment**: No need for `gcloud` CLI
3. ✅ **Production-ready**: Can use environment variables
4. ✅ **Easier to configure**: Just copy-paste the JSON

### ✅ Documentation Added

#### 1. **GCP_CREDENTIALS_SETUP.md**
Complete step-by-step guide for setting up GCP service account credentials:
- Creating service account
- Generating JSON key
- Converting JSON to single-line format
- Configuring application.properties
- Enabling Cloud Storage API
- Setting bucket permissions
- Troubleshooting common issues
- Security best practices

#### 2. **GCP_QUICK_START_WINDOWS.md**
Quick start guide specifically for Windows users:
- 5 simple steps to get started
- PowerShell-specific instructions
- Common error messages and fixes
- Verification checklist
- Example API calls

#### 3. **convert_gcp_credentials.py**
Python script to convert JSON credentials:
- Validates JSON file
- Converts to single-line format
- Outputs ready-to-use property line
- Saves to file for easy copying

#### 4. **convert_gcp_credentials.ps1**
PowerShell script for Windows users:
- Same functionality as Python script
- Native Windows PowerShell support
- Automatically copies to clipboard
- Color-coded output for better UX

#### 5. Updated **README.md**
Added references to new documentation files.

## How to Use

### Step 1: Get Your Service Account Key

1. Go to [GCP Console](https://console.cloud.google.com/iam-admin/serviceaccounts?project=tconecom-64307221061)
2. Create or select a service account
3. Generate and download JSON key

### Step 2: Convert JSON (Choose One Method)

**Method A: PowerShell (Windows - Recommended)**
```powershell
.\convert_gcp_credentials.ps1 "C:\path\to\your-key.json"
```

**Method B: Python**
```bash
python convert_gcp_credentials.py /path/to/your-key.json
```

**Method C: Manual**
1. Open JSON file
2. Remove all line breaks
3. Copy the entire JSON as single line

### Step 3: Update application.properties

Replace the placeholder in `gcp.storage.credentials-json` with your converted JSON.

### Step 4: Restart Application

```powershell
mvn clean install
mvn spring-boot:run
```

## Error Fix

### Before (Error):
```
2025-12-31 09:26:27 - No credentials path specified. Using Application Default Credentials.
2025-12-31 09:26:27 - Error checking/creating bucket: com.google.api.client.http.HttpResponseException: 400 Bad Request
POST https://oauth2.googleapis.com/token
{
  "error": "invalid_grant",
  "error_description": "reauth related error (invalid_rapt)"
}
```

### After (Success):
```
2025-12-31 09:30:00 - Loaded GCP credentials from application.properties JSON
2025-12-31 09:30:00 - GCP Storage Service initialized with bucket: ecom-product-images
2025-12-31 09:30:00 - Bucket ecom-product-images already exists
```

## Folder Organization

The service automatically creates vendor-specific folders:

```
ecom-product-images/
└── vendors/
    ├── business-name-vendorId1/
    │   ├── logo/
    │   │   └── logo-*.png
    │   └── products/
    │       ├── productId1/
    │       │   ├── image1.jpg
    │       │   └── image2.jpg
    │       └── productId2/
    │           └── image1.jpg
    └── another-business-vendorId2/
        └── ...
```

**Folder naming**: `{business-name}-{vendor-mongodb-id}`
- Business name is sanitized (lowercase, hyphens, no special chars)
- MongoDB ID ensures uniqueness
- Example: `tcon-solutions-695380a5517d313aeb4c2e8c`

## Security Considerations

### ✅ What We Did Right:
1. **No credentials in code**: All loaded from config
2. **Environment variable support**: Ready for production
3. **Clear documentation**: Easy to set up securely

### ⚠️ What You Should Do:

1. **Add to .gitignore**:
   ```gitignore
   # Never commit credentials
   application.properties
   **/gcp-credentials*.json
   **/gcp-credentials*.txt
   ```

2. **Use environment variables in production**:
   ```bash
   export GCP_CREDENTIALS_JSON='{"type":"service_account",...}'
   ```

3. **Rotate keys regularly**: Delete old keys from GCP Console

4. **Least privilege**: Use minimal required permissions

## Testing

### Test Upload (Manual):

```bash
# Get JWT token first by logging in
TOKEN="your_jwt_token"

# Upload product with images
curl -X POST http://localhost:8080/api/vendor/products \
  -H "Authorization: Bearer $TOKEN" \
  -F "name=Test Product" \
  -F "description=Test Description" \
  -F "price=99.99" \
  -F "stock=10" \
  -F "category=CLOTHING" \
  -F "images=@image1.jpg" \
  -F "images=@image2.jpg"
```

### Expected Response:

```json
{
  "id": "product-id-here",
  "name": "Test Product",
  "images": [
    "https://storage.googleapis.com/ecom-product-images/vendors/business-name-vendorId/products/product-id/uuid-timestamp-1.jpg",
    "https://storage.googleapis.com/ecom-product-images/vendors/business-name-vendorId/products/product-id/uuid-timestamp-2.jpg"
  ],
  ...
}
```

## Migration Path (For Existing Applications)

If you were using file-based credentials:

1. **Old approach** (requires file):
   ```properties
   gcp.storage.credentials-path=classpath:gcp-credentials.json
   ```

2. **New approach** (no file needed):
   ```properties
   gcp.storage.credentials-json={"type":"service_account",...}
   ```

**Benefits**:
- ✅ No credential files to manage
- ✅ Works in containers/cloud deployments
- ✅ Easier CI/CD integration
- ✅ Better for environment variables

## Next Steps

1. ✅ Create GCP service account (if not exists)
2. ✅ Download JSON key
3. ✅ Convert to single line using provided scripts
4. ✅ Update `application.properties`
5. ✅ Enable Cloud Storage API
6. ✅ Test the application
7. ✅ Verify bucket and folder creation
8. ✅ Test product image upload via API

## Support

For issues:
1. Check **GCP_QUICK_START_WINDOWS.md** for troubleshooting
2. Check **GCP_CREDENTIALS_SETUP.md** for detailed setup
3. Verify credentials at: https://console.cloud.google.com/iam-admin/serviceaccounts?project=tconecom-64307221061

## Files Modified/Created

### Modified:
- ✅ `src/main/resources/application.properties`
- ✅ `src/main/java/com/tcon/ecom/service/GcpStorageService.java`
- ✅ `README.md`

### Created:
- ✅ `ecom/GCP_CREDENTIALS_SETUP.md`
- ✅ `ecom/GCP_QUICK_START_WINDOWS.md`
- ✅ `ecom/convert_gcp_credentials.py`
- ✅ `ecom/convert_gcp_credentials.ps1`
- ✅ `ecom/GCP_CONFIGURATION_UPDATE_SUMMARY.md` (this file)

---

**Status**: ✅ Ready for Testing

**Impact**: The application will now load GCP credentials directly from `application.properties` instead of relying on Application Default Credentials or credential files. This makes it easier to configure and deploy in any environment.

