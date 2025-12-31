# ✅ GCP Storage Integration - Complete & Ready

## Status: **FIXED** ✅

The GCP authentication issue has been resolved. The application now loads credentials directly from `application.properties` instead of relying on Application Default Credentials.

---

## What Was Fixed

### 🔧 Problem
```
Error: 400 Bad Request
{
  "error": "invalid_grant",
  "error_description": "reauth related error (invalid_rapt)"
}
```

**Root Cause**: Application was trying to use Application Default Credentials (ADC) which requires `gcloud auth` CLI authentication.

### ✅ Solution
Changed to load credentials directly from JSON in `application.properties` - no external dependencies needed.

---

## Files Modified

1. **`src/main/resources/application.properties`**
   - Changed: `gcp.storage.credentials-path` → `gcp.storage.credentials-json`
   - Now accepts JSON credentials as a single-line string

2. **`src/main/java/com/tcon/ecom/service/GcpStorageService.java`**
   - Removed: Application Default Credentials fallback
   - Added: Direct JSON string credential loading
   - Fixed: All compilation errors

---

## Next Steps (Required)

### 1️⃣ Get Your GCP Service Account Key

Visit: https://console.cloud.google.com/iam-admin/serviceaccounts?project=tconecom-64307221061

- **If you DON'T have a service account yet:**
  1. Click **Create Service Account**
  2. Name: `ecom-storage-service`
  3. Role: **Storage Admin**
  4. Click **Create** → **Done**

- **Create JSON Key:**
  1. Click on the service account
  2. **Keys** tab → **Add Key** → **Create new key**
  3. Format: **JSON**
  4. Download the file (e.g., `tconecom-64307221061-abc123.json`)

### 2️⃣ Convert JSON to Single Line

**Windows PowerShell (Easiest):**
```powershell
cd C:\Users\dhanu\ecom\ecom-be\ecom
.\convert_gcp_credentials.ps1 "C:\Users\dhanu\Downloads\tconecom-64307221061-abc123.json"
```

This will:
- ✅ Validate your JSON
- ✅ Convert to single line
- ✅ Copy to clipboard automatically
- ✅ Save to `gcp-credentials-single-line.txt`

**Alternative - Python:**
```bash
python convert_gcp_credentials.py C:\Users\dhanu\Downloads\tconecom-64307221061-abc123.json
```

**Alternative - Online:**
1. Go to: https://codebeautify.org/json-minify
2. Paste your JSON
3. Click **Minify**
4. Copy result

### 3️⃣ Update application.properties

1. Open: `src/main/resources/application.properties`
2. Find line 53 (starts with `gcp.storage.credentials-json=`)
3. Replace everything after `=` with your converted JSON

**Before:**
```properties
gcp.storage.credentials-json={"type":"service_account","project_id":"tconecom-64307221061","private_key_id":"your-private-key-id"...}
```

**After:**
```properties
gcp.storage.credentials-json={"type":"service_account","project_id":"tconecom-64307221061","private_key_id":"abc123real...","private_key":"-----BEGIN PRIVATE KEY-----\nMIIE...real key here...\n-----END PRIVATE KEY-----\n"...}
```

⚠️ **Important**: Just paste the raw JSON - no escaping needed!

### 4️⃣ Enable Cloud Storage API

Visit: https://console.cloud.google.com/apis/library/storage.googleapis.com?project=tconecom-64307221061

Click **Enable** (if not already enabled)

### 5️⃣ Test the Application

```powershell
cd C:\Users\dhanu\ecom\ecom-be\ecom

# Clean build
mvn clean install

# Run
mvn spring-boot:run
```

**Expected Success Logs:**
```
✅ Loaded GCP credentials from application.properties JSON
✅ GCP Storage Service initialized with bucket: ecom-product-images
✅ Bucket ecom-product-images already exists
```

---

## Folder Structure Created

After uploading your first product image:

```
ecom-product-images/                    (GCP bucket)
└── vendors/
    └── your-business-name-695380a5517d313aeb4c2e8c/
        ├── logo/
        │   └── logo-uuid-timestamp.png
        └── products/
            ├── product-id-1/
            │   ├── uuid-timestamp-1.jpg
            │   └── uuid-timestamp-2.jpg
            └── product-id-2/
                └── uuid-timestamp-3.jpg
```

**Folder naming convention:**
- Format: `{sanitized-business-name}-{vendor-mongodb-id}`
- Example: `tcon-solutions-695380a5517d313aeb4c2e8c`
- Business name is lowercase, spaces → hyphens, special chars removed

---

## API Usage Example

### Upload Product with Images

```bash
# First, login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"vendor@example.com","password":"password123"}'

# Response contains: {"accessToken":"eyJhbGc...","refreshToken":"..."}

# Upload product with images
curl -X POST http://localhost:8080/api/vendor/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -F "name=Premium T-Shirt" \
  -F "description=High quality cotton t-shirt" \
  -F "price=29.99" \
  -F "stock=100" \
  -F "category=CLOTHING" \
  -F "images=@image1.jpg" \
  -F "images=@image2.jpg"
```

**Response:**
```json
{
  "id": "product-id-here",
  "name": "Premium T-Shirt",
  "images": [
    "https://storage.googleapis.com/ecom-product-images/vendors/your-business-695380a5.../products/product-id/uuid-1.jpg",
    "https://storage.googleapis.com/ecom-product-images/vendors/your-business-695380a5.../products/product-id/uuid-2.jpg"
  ],
  "price": 29.99,
  ...
}
```

---

## Troubleshooting

### ❌ "Invalid GCP credentials JSON"
**Fix**: 
- Make sure JSON is on ONE line
- Paste raw JSON (no escape characters needed in properties file)
- Validate JSON at: https://jsonlint.com/

### ❌ "Error checking/creating bucket: 403 Forbidden"
**Fix**:
1. Go to: https://console.cloud.google.com/iam-admin/serviceaccounts?project=tconecom-64307221061
2. Click your service account → **Permissions**
3. Grant **Storage Admin** role

### ❌ "Failed to initialize GCP Storage"
**Fix**:
- Check project ID is correct: `tconecom-64307221061`
- Verify Cloud Storage API is enabled
- Ensure service account JSON is valid

### ❌ Still getting "invalid_grant" error
**Fix**:
1. Delete the old service account key
2. Create a new JSON key
3. Convert it again and update application.properties
4. Restart application

---

## Security Checklist

- [ ] Add `application.properties` to `.gitignore`
- [ ] Add `**/gcp-credentials*.json` to `.gitignore`
- [ ] Add `**/gcp-credentials*.txt` to `.gitignore`
- [ ] Never commit credentials to Git
- [ ] Use environment variables in production
- [ ] Rotate service account keys every 90 days

**Production Environment Variables:**
```bash
# Set environment variable
export GCP_CREDENTIALS_JSON='{"type":"service_account",...}'

# In application.properties, it will automatically pick it up:
gcp.storage.credentials-json=${GCP_CREDENTIALS_JSON}
```

---

## Documentation Files

📚 **Detailed Guides Created:**

1. **`GCP_CREDENTIALS_SETUP.md`** - Complete setup guide
2. **`GCP_QUICK_START_WINDOWS.md`** - Quick start for Windows users
3. **`GCP_CONFIGURATION_UPDATE_SUMMARY.md`** - What changed & why
4. **`convert_gcp_credentials.ps1`** - PowerShell conversion script
5. **`convert_gcp_credentials.py`** - Python conversion script

---

## Test Verification

After setup, verify with these steps:

1. ✅ Application starts without errors
2. ✅ Logs show: "Loaded GCP credentials from application.properties JSON"
3. ✅ Logs show: "GCP Storage Service initialized with bucket: ecom-product-images"
4. ✅ Bucket created in GCP Console: https://console.cloud.google.com/storage/browser?project=tconecom-64307221061
5. ✅ Upload test product image via API
6. ✅ Verify image URL is accessible
7. ✅ Check folder structure in GCP bucket

---

## Quick Reference

| Item | Value |
|------|-------|
| **Project ID** | `tconecom-64307221061` |
| **Bucket Name** | `ecom-product-images` |
| **Region** | `ASIA-SOUTH1` (Mumbai) |
| **Service Account** | `ecom-storage-service@tconecom-64307221061.iam.gserviceaccount.com` |
| **Required Role** | Storage Admin |
| **API Endpoint** | `/api/vendor/products` |

---

## Support Resources

- 📖 [Google Cloud Storage Docs](https://cloud.google.com/storage/docs)
- 🔐 [Service Account Docs](https://cloud.google.com/iam/docs/service-accounts)
- 🛠️ [GCP Console](https://console.cloud.google.com/home/dashboard?project=tconecom-64307221061)
- 📧 [GCP Support](https://cloud.google.com/support)

---

## Summary

✅ **Code**: Fixed and compiles successfully  
✅ **Documentation**: Complete guides created  
✅ **Scripts**: PowerShell and Python converters ready  
✅ **Configuration**: Updated to use JSON credentials  

**Status**: Ready to configure credentials and test! 🚀

---

**Next Action**: Follow steps 1-5 above to complete the setup.

