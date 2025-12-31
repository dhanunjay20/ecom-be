# Quick Start: Setting Up GCP Storage Credentials (Windows)

## For Busy Developers 🚀

Follow these 5 simple steps to get GCP storage working:

### Step 1: Download Your Service Account Key

1. Go to: https://console.cloud.google.com/iam-admin/serviceaccounts?project=tconecom-64307221061
2. If you don't have a service account yet:
   - Click **Create Service Account**
   - Name: `ecom-storage-service`
   - Role: **Storage Admin**
   - Click **Create** → **Done**
3. Click on the service account
4. Go to **Keys** tab → **Add Key** → **Create new key** → **JSON**
5. Save the downloaded JSON file (e.g., `tconecom-64307221061-abc123.json`)

### Step 2: Convert JSON to Single Line

**Option A: Using PowerShell Script (Recommended for Windows)**

```powershell
# Run this in PowerShell (in the ecom folder)
.\convert_gcp_credentials.ps1 "C:\Users\YourName\Downloads\tconecom-64307221061-abc123.json"
```

This will:
- ✅ Validate your JSON file
- ✅ Convert it to a single line
- ✅ Copy to clipboard automatically
- ✅ Save to `gcp-credentials-single-line.txt`

**Option B: Using Python**

```bash
# If you have Python installed
python convert_gcp_credentials.py C:\Users\YourName\Downloads\tconecom-64307221061-abc123.json
```

**Option C: Online Tool**

1. Go to: https://codebeautify.org/json-minify
2. Paste your JSON file content
3. Click **Minify**
4. Copy the result

### Step 3: Update application.properties

1. Open `src/main/resources/application.properties`
2. Find the line starting with `gcp.storage.credentials-json=`
3. Paste the single-line JSON from Step 2

Example:
```properties
gcp.storage.credentials-json={"type":"service_account","project_id":"tconecom-64307221061","private_key_id":"abc123...","private_key":"-----BEGIN PRIVATE KEY-----\nMIIEvQIBADAN...\n-----END PRIVATE KEY-----\n","client_email":"ecom-storage-service@tconecom-64307221061.iam.gserviceaccount.com",...}
```

### Step 4: Enable Cloud Storage API

1. Go to: https://console.cloud.google.com/apis/library/storage.googleapis.com?project=tconecom-64307221061
2. Click **Enable** (if not already enabled)

### Step 5: Test It!

Restart your application:

```powershell
# Clean build
mvn clean install

# Run the application
mvn spring-boot:run
```

Look for these success messages in the console:
```
✅ Loaded GCP credentials from application.properties JSON
✅ GCP Storage Service initialized with bucket: ecom-product-images
✅ Bucket ecom-product-images already exists
```

## ⚠️ Troubleshooting

### Error: "Invalid GCP credentials JSON"

**Cause**: JSON formatting issue

**Fix**: 
1. Make sure the JSON is on ONE LINE (no line breaks)
2. Check that quotes are NOT escaped in application.properties (just paste the raw JSON)
3. Verify the JSON is valid: https://jsonlint.com/

### Error: "Error checking/creating bucket"

**Cause**: Insufficient permissions

**Fix**:
1. Go to: https://console.cloud.google.com/iam-admin/serviceaccounts?project=tconecom-64307221061
2. Click on your service account
3. Click **Permissions** tab → **Grant Access**
4. Role: **Storage Admin**
5. Click **Save**

### Error: "400 Bad Request - invalid_grant"

**Cause**: Wrong credentials or expired key

**Fix**:
1. Delete the old service account key
2. Create a new one (Step 1)
3. Repeat Steps 2-3 with the new key

### Error: "Project not found"

**Cause**: Wrong project ID

**Fix**:
1. Check your project ID at: https://console.cloud.google.com/home/dashboard
2. Update in `application.properties`:
   ```properties
   gcp.storage.project-id=YOUR_ACTUAL_PROJECT_ID
   ```

## 🔒 Security Tips

1. **Never commit credentials to Git!**
   
   Add to `.gitignore`:
   ```gitignore
   application.properties
   **/gcp-credentials*.json
   **/gcp-credentials*.txt
   ```

2. **Use environment variables for production**
   
   In Windows PowerShell:
   ```powershell
   $env:GCP_CREDENTIALS_JSON = Get-Content gcp-credentials-single-line.txt
   mvn spring-boot:run
   ```
   
   Then in `application.properties`:
   ```properties
   gcp.storage.credentials-json=${GCP_CREDENTIALS_JSON}
   ```

3. **Rotate keys regularly**
   
   Delete old keys from: https://console.cloud.google.com/iam-admin/serviceaccounts?project=tconecom-64307221061

## 📦 What Gets Created?

After uploading a product image, the folder structure will be:

```
ecom-product-images/  (GCP bucket)
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

## ✅ Verification Checklist

- [ ] Service account created with **Storage Admin** role
- [ ] JSON key downloaded
- [ ] JSON converted to single line
- [ ] `application.properties` updated with credentials
- [ ] Cloud Storage API enabled
- [ ] Application starts without errors
- [ ] Logs show "GCP Storage Service initialized"

## 📞 Still Having Issues?

Check the detailed guide: [GCP_CREDENTIALS_SETUP.md](GCP_CREDENTIALS_SETUP.md)

## 🎉 You're Done!

Your GCP storage is now configured. You can:
- Upload product images via vendor portal
- Images are automatically organized by vendor
- Each vendor gets their own folder: `businessname-vendorid`
- Public URLs are generated automatically

Example API call to upload:
```bash
curl -X POST http://localhost:8080/api/vendor/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "name=Product Name" \
  -F "price=99.99" \
  -F "images=@product-image1.jpg" \
  -F "images=@product-image2.jpg"
```

Happy coding! 🚀

