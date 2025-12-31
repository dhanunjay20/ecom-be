# GCP Storage Module - Quick Start Guide

## Overview

This module handles all file uploads for the e-commerce platform using Google Cloud Storage. It automatically organizes files into vendor-specific folders based on their business name and MongoDB ID.

## Features

✅ **Vendor-specific folders**: Each vendor gets a unique folder named `{business-name}-{vendor-id}`
✅ **Product image uploads**: Single and multiple file uploads
✅ **Vendor logo uploads**: Dedicated logo management
✅ **File validation**: Automatic validation for file size (5MB max) and type (images only)
✅ **Public access**: Files are automatically made public
✅ **File management**: Delete single files, multiple files, or entire vendor folders
✅ **Metadata retrieval**: Get file information and statistics
✅ **Secure**: JWT authentication required for all operations

## Folder Structure

```
ecom-product-images/ (GCP Bucket)
└── vendors/
    └── tcon-solutions-695380a5517d313aeb4c2e8c/
        ├── logo/
        │   └── logo-uuid-timestamp.png
        └── products/
            ├── product-id-1/
            │   ├── uuid-timestamp.jpg
            │   └── uuid-timestamp.jpg
            └── product-id-2/
                └── uuid-timestamp.jpg
```

## Setup Instructions

### 1. Set up GCP Account and Credentials

Follow the detailed guide in `GCP_STORAGE_SETUP.md`

### 2. Configure Application

Update `src/main/resources/application.properties`:

```properties
gcp.storage.project-id=your-gcp-project-id
gcp.storage.bucket-name=ecom-product-images
gcp.storage.credentials-path=classpath:gcp-credentials.json
gcp.storage.base-url=https://storage.googleapis.com
```

### 3. Add GCP Credentials

1. Download your service account JSON key from GCP Console
2. Rename it to `gcp-credentials.json`
3. Place it in `src/main/resources/`
4. **Important**: This file is already in `.gitignore` - never commit it!

## API Usage Examples

### 1. Upload Single Product Image

**Endpoint**: `POST /api/storage/upload/product-image`

**Headers**:
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: multipart/form-data
```

**Form Data**:
- `file`: Image file (required)
- `vendorId`: Vendor's MongoDB ID (required)
- `productId`: Product's MongoDB ID (optional)

**Example using cURL**:
```bash
curl -X POST http://localhost:8080/api/storage/upload/product-image \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/image.jpg" \
  -F "vendorId=695380a5517d313aeb4c2e8c" \
  -F "productId=6953a0a5517d313aeb4c2e8d"
```

**Response**:
```json
{
  "fileUrl": "https://storage.googleapis.com/ecom-product-images/vendors/tcon-solutions-695380a5517d313aeb4c2e8c/products/6953a0a5517d313aeb4c2e8d/uuid-timestamp.jpg",
  "fileName": "product-image.jpg",
  "fileType": "image/jpeg",
  "fileSize": 245678,
  "vendorFolder": "tcon-solutions-695380a5517d313aeb4c2e8c",
  "message": "File uploaded successfully"
}
```

### 2. Upload Multiple Product Images

**Endpoint**: `POST /api/storage/upload/product-images`

**Form Data**:
- `files`: Array of image files (required)
- `vendorId`: Vendor's MongoDB ID (required)
- `productId`: Product's MongoDB ID (optional)

**Example using cURL**:
```bash
curl -X POST http://localhost:8080/api/storage/upload/product-images \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "files=@/path/to/image1.jpg" \
  -F "files=@/path/to/image2.jpg" \
  -F "files=@/path/to/image3.jpg" \
  -F "vendorId=695380a5517d313aeb4c2e8c" \
  -F "productId=6953a0a5517d313aeb4c2e8d"
```

**Response**:
```json
{
  "fileUrls": [
    "https://storage.googleapis.com/.../image1.jpg",
    "https://storage.googleapis.com/.../image2.jpg",
    "https://storage.googleapis.com/.../image3.jpg"
  ],
  "uploadedCount": 3,
  "totalCount": 3,
  "vendorFolder": "tcon-solutions-695380a5517d313aeb4c2e8c",
  "message": "3 files uploaded successfully"
}
```

### 3. Upload Vendor Logo

**Endpoint**: `POST /api/storage/upload/vendor-logo`

**Form Data**:
- `file`: Image file (required)
- `vendorId`: Vendor's MongoDB ID (required)

**Example using cURL**:
```bash
curl -X POST http://localhost:8080/api/storage/upload/vendor-logo \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/logo.png" \
  -F "vendorId=695380a5517d313aeb4c2e8c"
```

### 4. Delete a File

**Endpoint**: `DELETE /api/storage/delete`

**Query Parameters**:
- `fileUrl`: Full URL of the file to delete

**Example using cURL**:
```bash
curl -X DELETE "http://localhost:8080/api/storage/delete?fileUrl=https://storage.googleapis.com/..." \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5. List Vendor Files

**Endpoint**: `GET /api/storage/list/vendor-files`

**Query Parameters**:
- `vendorId`: Vendor's MongoDB ID (required)
- `subFolder`: Subfolder name (optional, e.g., "products", "logo")

**Example using cURL**:
```bash
curl -X GET "http://localhost:8080/api/storage/list/vendor-files?vendorId=695380a5517d313aeb4c2e8c&subFolder=products" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 6. Get File Metadata

**Endpoint**: `GET /api/storage/metadata`

**Query Parameters**:
- `fileUrl`: Full URL of the file

**Example using cURL**:
```bash
curl -X GET "http://localhost:8080/api/storage/metadata?fileUrl=https://storage.googleapis.com/..." \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response**:
```json
{
  "name": "vendors/tcon-solutions-695380a5517d313aeb4c2e8c/products/uuid.jpg",
  "size": 245678,
  "contentType": "image/jpeg",
  "created": "2025-12-31T09:00:00Z",
  "updated": "2025-12-31T09:00:00Z",
  "url": "https://storage.googleapis.com/..."
}
```

## Frontend Integration Examples

### React/TypeScript Example

```typescript
// Upload single product image
const uploadProductImage = async (
  file: File,
  vendorId: string,
  productId?: string
) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('vendorId', vendorId);
  if (productId) {
    formData.append('productId', productId);
  }

  const response = await fetch('http://localhost:8080/api/storage/upload/product-image', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${yourJwtToken}`,
    },
    body: formData,
  });

  return await response.json();
};

// Upload multiple images
const uploadMultipleImages = async (
  files: File[],
  vendorId: string,
  productId?: string
) => {
  const formData = new FormData();
  files.forEach(file => {
    formData.append('files', file);
  });
  formData.append('vendorId', vendorId);
  if (productId) {
    formData.append('productId', productId);
  }

  const response = await fetch('http://localhost:8080/api/storage/upload/product-images', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${yourJwtToken}`,
    },
    body: formData,
  });

  return await response.json();
};

// Delete file
const deleteFile = async (fileUrl: string) => {
  const response = await fetch(
    `http://localhost:8080/api/storage/delete?fileUrl=${encodeURIComponent(fileUrl)}`,
    {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${yourJwtToken}`,
      },
    }
  );

  return await response.json();
};
```

### HTML Form Example

```html
<form id="uploadForm">
  <input type="file" id="fileInput" accept="image/*" multiple />
  <button type="submit">Upload Images</button>
</form>

<script>
document.getElementById('uploadForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  
  const files = document.getElementById('fileInput').files;
  const formData = new FormData();
  
  for (let file of files) {
    formData.append('files', file);
  }
  formData.append('vendorId', 'YOUR_VENDOR_ID');
  formData.append('productId', 'YOUR_PRODUCT_ID');
  
  const response = await fetch('http://localhost:8080/api/storage/upload/product-images', {
    method: 'POST',
    headers: {
      'Authorization': 'Bearer YOUR_JWT_TOKEN'
    },
    body: formData
  });
  
  const result = await response.json();
  console.log('Upload successful:', result);
});
</script>
```

## File Validation Rules

- **Maximum file size**: 5MB
- **Allowed formats**: JPEG, PNG, GIF, WEBP
- **File naming**: Automatic UUID + timestamp naming
- **Folder naming**: Sanitized business name + vendor ID

## Security & Access Control

### Role-Based Access

- **VENDOR role**: Can upload/delete their own files
- **ADMIN role**: Can delete any files and vendor folders

### Authentication

All endpoints require JWT authentication via Bearer token:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Error Handling

### Common Error Responses

**400 Bad Request**:
```json
{
  "success": false,
  "message": "File size exceeds maximum limit of 5MB"
}
```

**401 Unauthorized**:
```json
{
  "success": false,
  "message": "Unauthorized - Invalid or missing token"
}
```

**404 Not Found**:
```json
{
  "success": false,
  "message": "Vendor not found"
}
```

## Testing with Postman

1. **Create a new collection**: "GCP Storage API"

2. **Set up environment variables**:
   - `baseUrl`: `http://localhost:8080`
   - `jwtToken`: Your JWT token
   - `vendorId`: Your vendor ID

3. **Upload Product Image Request**:
   - Method: POST
   - URL: `{{baseUrl}}/api/storage/upload/product-image`
   - Headers: `Authorization: Bearer {{jwtToken}}`
   - Body: form-data
     - `file`: (select file)
     - `vendorId`: `{{vendorId}}`
     - `productId`: (optional)

## Production Deployment

For production on GCP Cloud Run or similar:

1. **Use environment variables** instead of hardcoded values
2. **Enable Application Default Credentials** (remove credentials-path)
3. **Set up CDN** for faster image delivery
4. **Configure CORS** for your frontend domain
5. **Enable lifecycle policies** to archive old images
6. **Monitor storage costs** and usage

## Troubleshooting

### Issue: "Failed to upload file"
- Check GCP credentials are valid
- Verify bucket exists and is accessible
- Check service account has correct permissions

### Issue: "File size exceeds limit"
- Compress images before upload
- Or modify validation in `GcpStorageService.validateFile()`

### Issue: "Invalid file type"
- Only images are allowed (JPEG, PNG, GIF, WEBP)
- Check file MIME type

## Next Steps

- [ ] Implement image resizing/optimization
- [ ] Add support for video uploads
- [ ] Implement CDN integration
- [ ] Add image compression before upload
- [ ] Set up automated backups

## Support

For questions or issues, contact the development team.

