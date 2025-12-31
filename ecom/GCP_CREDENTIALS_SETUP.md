# GCP Storage Credentials Setup Guide

## Prerequisites
- A Google Cloud Platform (GCP) account
- A GCP project created (your project: `tconecom-64307221061`)

## Step 1: Create a Service Account

1. Go to the [GCP Console](https://console.cloud.google.com/)
2. Select your project: `tconecom-64307221061`
3. Navigate to **IAM & Admin** > **Service Accounts**
4. Click **Create Service Account**
5. Fill in the details:
   - **Name**: `ecom-storage-service`
   - **Description**: `Service account for e-commerce product image storage`
6. Click **Create and Continue**
7. Grant the following role:
   - **Storage Admin** (for full bucket management)
   - OR **Storage Object Admin** (for object-level operations only)
8. Click **Continue** and then **Done**

## Step 2: Create and Download Service Account Key

1. Click on the newly created service account
2. Go to the **Keys** tab
3. Click **Add Key** > **Create new key**
4. Select **JSON** format
5. Click **Create**
6. The JSON key file will be downloaded to your computer

## Step 3: Configure application.properties

The JSON key file will look like this:

```json
{
  "type": "service_account",
  "project_id": "tconecom-64307221061",
  "private_key_id": "abc123...",
  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0B...\n-----END PRIVATE KEY-----\n",
  "client_email": "ecom-storage-service@tconecom-64307221061.iam.gserviceaccount.com",
  "client_id": "123456789",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/..."
}
```

### Convert JSON to Single Line

You need to convert this JSON to a single line and escape the quotes. Here's how:

#### Option A: Manual Conversion
1. Copy the entire JSON content
2. Remove all newlines and extra spaces
3. Replace `"` with `\"` (except for the outer quotes)
4. Replace actual newlines in the private_key with `\\n`

Example result:
```properties
gcp.storage.credentials-json={"type":"service_account","project_id":"tconecom-64307221061","private_key_id":"abc123...","private_key":"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0B...\\n-----END PRIVATE KEY-----\\n","client_email":"ecom-storage-service@tconecom-64307221061.iam.gserviceaccount.com","client_id":"123456789","auth_uri":"https://accounts.google.com/o/oauth2/auth","token_uri":"https://oauth2.googleapis.com/token","auth_provider_x509_cert_url":"https://www.googleapis.com/oauth2/v1/certs","client_x509_cert_url":"https://www.googleapis.com/robot/v1/metadata/x509/..."}
```

#### Option B: Using Python
```python
import json

# Read the downloaded JSON file
with open('path/to/your-service-account-key.json', 'r') as f:
    data = json.load(f)

# Convert to single line (already escaped)
single_line = json.dumps(data, separators=(',', ':'))
print(single_line)
```

#### Option C: Using Node.js
```javascript
const fs = require('fs');

// Read the downloaded JSON file
const data = JSON.parse(fs.readFileSync('path/to/your-service-account-key.json', 'utf8'));

// Convert to single line
const singleLine = JSON.stringify(data);
console.log(singleLine);
```

### Update application.properties

Open `src/main/resources/application.properties` and update the `gcp.storage.credentials-json` property with your single-line JSON:

```properties
# Google Cloud Storage Configuration
gcp.storage.project-id=tconecom-64307221061
gcp.storage.bucket-name=ecom-product-images
gcp.storage.base-url=https://storage.googleapis.com
# GCP Service Account Credentials (JSON key as single line)
gcp.storage.credentials-json={"type":"service_account","project_id":"tconecom-64307221061",...YOUR_COMPLETE_JSON_HERE...}
```

## Step 4: Enable Cloud Storage API

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Navigate to **APIs & Services** > **Library**
3. Search for **Cloud Storage API**
4. Click **Enable** if not already enabled

## Step 5: Create Storage Bucket (Optional)

The application will automatically create the bucket `ecom-product-images` if it doesn't exist. However, you can create it manually:

1. Go to **Cloud Storage** > **Buckets**
2. Click **Create Bucket**
3. Name: `ecom-product-images`
4. Location type: **Region**
5. Location: **asia-south1 (Mumbai)**
6. Storage class: **Standard**
7. Access control: **Fine-grained**
8. Click **Create**

## Step 6: Set Bucket Permissions (for public access)

To make uploaded images publicly accessible:

1. Go to your bucket: `ecom-product-images`
2. Click on **Permissions** tab
3. Click **Grant Access**
4. Add principal: `allUsers`
5. Role: **Storage Object Viewer**
6. Click **Save**

**Warning**: This makes all objects in the bucket publicly readable. For production, consider using signed URLs instead.

## Step 7: Test the Configuration

Restart your application and check the logs:

```
2025-12-31 09:26:27 - Loaded GCP credentials from application.properties JSON
2025-12-31 09:26:27 - GCP Storage Service initialized with bucket: ecom-product-images
2025-12-31 09:26:27 - Bucket ecom-product-images already exists
```

If successful, you should see these log messages without errors.

## Troubleshooting

### Error: "Invalid GCP credentials JSON"
- Make sure the JSON is properly formatted as a single line
- Check that all `"` characters are escaped
- Verify that newlines in the private_key are `\\n` (double backslash)

### Error: "Failed to initialize GCP Storage"
- Verify the project ID is correct
- Ensure the Cloud Storage API is enabled
- Check that the service account has the correct permissions

### Error: "Bucket creation failed"
- The service account needs **Storage Admin** role
- Check bucket naming rules (lowercase, no underscores, etc.)

### Error: "403 Forbidden" when uploading
- The service account needs **Storage Object Admin** or **Storage Admin** role
- Check bucket IAM permissions

## Security Best Practices

1. **Never commit credentials to Git**: Add `application.properties` to `.gitignore`
2. **Use environment variables** for production:
   ```bash
   export GCP_CREDENTIALS_JSON='{"type":"service_account",...}'
   ```
   Then in `application.properties`:
   ```properties
   gcp.storage.credentials-json=${GCP_CREDENTIALS_JSON}
   ```
3. **Rotate service account keys** periodically
4. **Use least privilege**: Grant only necessary permissions
5. **Monitor API usage**: Set up billing alerts in GCP Console

## Folder Structure

The application automatically organizes files by vendor:

```
ecom-product-images/
└── vendors/
    ├── tcon-solutions-695380a5517d313aeb4c2e8c/
    │   ├── logo/
    │   │   └── logo-uuid-timestamp.png
    │   └── products/
    │       ├── product-id-1/
    │       │   ├── uuid-timestamp-1.jpg
    │       │   └── uuid-timestamp-2.jpg
    │       └── product-id-2/
    │           └── uuid-timestamp-3.jpg
    └── another-vendor-695380a5517d313aeb4c2e8d/
        └── products/
            └── ...
```

## Need Help?

- [GCP Service Accounts Documentation](https://cloud.google.com/iam/docs/service-accounts)
- [Cloud Storage API Documentation](https://cloud.google.com/storage/docs)
- [GCP IAM Roles](https://cloud.google.com/storage/docs/access-control/iam-roles)

