# GCP Storage Setup Guide

This guide will help you set up Google Cloud Storage for the e-commerce application.

## Prerequisites

- Google Cloud Platform account
- GCP project created
- Billing enabled on your GCP project

## Steps to Set Up GCP Storage

### 1. Create a GCP Project (if not already created)

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click on the project dropdown at the top
3. Click "New Project"
4. Enter a project name (e.g., "ecom-backend")
5. Click "Create"

### 2. Enable Cloud Storage API

1. In the GCP Console, go to "APIs & Services" > "Library"
2. Search for "Cloud Storage API"
3. Click on it and click "Enable"

### 3. Create a Storage Bucket

1. Go to "Cloud Storage" > "Buckets" in the GCP Console
2. Click "Create Bucket"
3. Configure bucket:
   - Name: `ecom-product-images` (or your preferred name)
   - Location type: Region
   - Location: `asia-south1` (Mumbai) - or closest to your users
   - Storage class: Standard
   - Access control: Fine-grained
   - Protection tools: None (or as needed)
4. Click "Create"

### 4. Create a Service Account

1. Go to "IAM & Admin" > "Service Accounts"
2. Click "Create Service Account"
3. Enter details:
   - Service account name: `ecom-storage-service`
   - Service account ID: (auto-generated)
   - Description: "Service account for ecom storage operations"
4. Click "Create and Continue"
5. Grant roles:
   - Select "Storage Admin" role (or "Storage Object Admin" for more restrictive access)
6. Click "Continue" and then "Done"

### 5. Generate Service Account Key

1. Find your newly created service account in the list
2. Click on the three dots (⋮) and select "Manage keys"
3. Click "Add Key" > "Create new key"
4. Select "JSON" as the key type
5. Click "Create"
6. The JSON key file will be downloaded to your computer

### 6. Configure the Application

1. **Save the JSON key file:**
   - Rename the downloaded file to `gcp-credentials.json`
   - Copy it to `src/main/resources/` directory in your project
   - **Important:** Add this file to `.gitignore` to prevent committing credentials

2. **Update `application.properties`:**

```properties
# Google Cloud Storage Configuration
gcp.storage.project-id=your-gcp-project-id
gcp.storage.bucket-name=ecom-product-images
gcp.storage.credentials-path=classpath:gcp-credentials.json
gcp.storage.base-url=https://storage.googleapis.com
```

Replace:
- `your-gcp-project-id` with your actual GCP project ID (found in the JSON file under "project_id")
- `ecom-product-images` with your bucket name if different

### 7. Set Up Bucket Permissions (Optional - for public access)

If you want uploaded images to be publicly accessible:

1. Go to your bucket in GCP Console
2. Click on "Permissions" tab
3. Click "Add"
4. Add:
   - New principals: `allUsers`
   - Role: `Storage Object Viewer`
5. Click "Save"

**Note:** The application automatically sets files as public when uploading. Skip this step if you prefer programmatic control.

### 8. Update .gitignore

Add the following to your `.gitignore` file:

```
# GCP Credentials
src/main/resources/gcp-credentials.json
gcp-credentials.json
```

### 9. For Production Deployment (GCP Cloud Run / Compute Engine)

When deploying to GCP services, you can use Application Default Credentials instead of a JSON file:

1. Update `application.properties` for production:

```properties
# Google Cloud Storage Configuration (Production)
gcp.storage.project-id=${GCP_PROJECT_ID}
gcp.storage.bucket-name=${GCP_BUCKET_NAME}
gcp.storage.credentials-path=
gcp.storage.base-url=https://storage.googleapis.com
```

2. Set environment variables in your deployment:
   - `GCP_PROJECT_ID`: Your GCP project ID
   - `GCP_BUCKET_NAME`: Your bucket name

3. Ensure the service account attached to your Cloud Run/Compute Engine instance has the necessary permissions.

## Folder Structure in Bucket

The application organizes files as follows:

```
ecom-product-images/
├── vendors/
│   ├── {business-name}-{vendor-id}/
│   │   ├── logo/
│   │   │   └── logo-{uuid}.jpg
│   │   └── products/
│   │       ├── {product-id}/
│   │       │   ├── {uuid}-{timestamp}.jpg
│   │       │   └── {uuid}-{timestamp}.jpg
│   │       └── {uuid}-{timestamp}.jpg (without product ID)
```

### Example:
```
vendors/
  tcon-solutions-695380a5517d313aeb4c2e8c/
    logo/
      logo-abc123-1704012345678.png
    products/
      6953a0a5517d313aeb4c2e8d/
        uuid1-1704012345678.jpg
        uuid2-1704012345679.jpg
```

## API Endpoints

### Upload Product Image
```http
POST /api/storage/upload/product-image
Content-Type: multipart/form-data

Parameters:
- file: Image file
- vendorId: Vendor's MongoDB ID
- productId: (Optional) Product's MongoDB ID
```

### Upload Multiple Product Images
```http
POST /api/storage/upload/product-images
Content-Type: multipart/form-data

Parameters:
- files: Array of image files
- vendorId: Vendor's MongoDB ID
- productId: (Optional) Product's MongoDB ID
```

### Upload Vendor Logo
```http
POST /api/storage/upload/vendor-logo
Content-Type: multipart/form-data

Parameters:
- file: Image file
- vendorId: Vendor's MongoDB ID
```

### Delete File
```http
DELETE /api/storage/delete?fileUrl={url}
```

### List Vendor Files
```http
GET /api/storage/list/vendor-files?vendorId={id}&subFolder={folder}
```

## Testing

### Using cURL:

```bash
# Upload product image
curl -X POST http://localhost:8080/api/storage/upload/product-image \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/image.jpg" \
  -F "vendorId=695380a5517d313aeb4c2e8c" \
  -F "productId=6953a0a5517d313aeb4c2e8d"

# Upload vendor logo
curl -X POST http://localhost:8080/api/storage/upload/vendor-logo \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/logo.png" \
  -F "vendorId=695380a5517d313aeb4c2e8c"
```

### Using Postman:

1. Create a new POST request to `http://localhost:8080/api/storage/upload/product-image`
2. Add Authorization header with Bearer token
3. Go to Body tab > form-data
4. Add fields:
   - `file` (type: File) - select an image
   - `vendorId` (type: Text) - enter vendor ID
   - `productId` (type: Text) - enter product ID (optional)
5. Send request

## File Validation

The service validates:
- File size: Maximum 5MB
- File types: JPEG, PNG, GIF, WEBP
- File presence: Cannot be empty

## Security

- All endpoints require authentication (JWT token)
- Product image upload requires VENDOR role
- Vendor logo upload requires VENDOR role
- Delete operations require VENDOR or ADMIN role
- Vendor folder deletion requires ADMIN role

## Troubleshooting

### Error: "The Application Default Credentials are not available"

**Solution:** Ensure `gcp-credentials.json` is in the correct location and `gcp.storage.credentials-path` is set correctly.

### Error: "403 Forbidden"

**Solution:** 
- Check that the service account has the correct permissions (Storage Admin or Storage Object Admin)
- Verify the bucket name is correct

### Error: "Bucket does not exist"

**Solution:** 
- Ensure the bucket is created in GCP Console
- Verify the bucket name in `application.properties` matches the GCP bucket name

### Error: "File size exceeds maximum limit"

**Solution:** 
- Reduce image size to under 5MB
- Or update the validation in `GcpStorageService.validateFile()` method

## Cost Optimization

- **Storage class:** Use "Standard" for frequently accessed images
- **Lifecycle policies:** Set up lifecycle rules to archive old images
- **CDN:** Consider using Cloud CDN for faster image delivery
- **Image optimization:** Compress images before uploading

## Next Steps

1. Set up Cloud CDN for faster image delivery
2. Implement image resizing/optimization
3. Add image compression before upload
4. Set up lifecycle policies for old images
5. Monitor storage usage and costs

## Support

For issues or questions, contact the development team.

