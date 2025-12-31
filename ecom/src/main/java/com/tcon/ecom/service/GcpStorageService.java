package com.tcon.ecom.service;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import com.tcon.ecom.exception.BadRequestException;
import com.tcon.ecom.model.Vendor;
import com.tcon.ecom.repository.VendorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@ConditionalOnProperty(name = "gcp.storage.enabled", havingValue = "true", matchIfMissing = false)
public class GcpStorageService {

    private final Storage storage;
    private final String bucketName;
    private final String baseUrl;
    private final VendorRepository vendorRepository;

    public GcpStorageService(
            @Value("${gcp.storage.project-id}") String projectId,
            @Value("${gcp.storage.bucket-name}") String bucketName,
            @Value("${gcp.storage.base-url}") String baseUrl,
            @Value("${gcp.storage.credentials-json:#{null}}") String credentialsJson,
            VendorRepository vendorRepository) throws IOException {

        this.bucketName = bucketName;
        this.baseUrl = baseUrl;
        this.vendorRepository = vendorRepository;

        log.info("Initializing GCP Storage Service...");

        // Initialize Storage client
        StorageOptions.Builder builder = StorageOptions.newBuilder().setProjectId(projectId);

        // Load credentials from JSON string in properties
        if (credentialsJson != null && !credentialsJson.isEmpty() && !credentialsJson.equals("#{null}")) {
            try {
                // Convert JSON string to InputStream
                java.io.ByteArrayInputStream credStream = new java.io.ByteArrayInputStream(
                    credentialsJson.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                );

                builder.setCredentials(
                    com.google.auth.oauth2.GoogleCredentials.fromStream(credStream)
                );
                log.info("✅ Loaded GCP credentials from application.properties JSON");
            } catch (Exception e) {
                log.error("❌ Failed to load GCP credentials from JSON: {}", e.getMessage());
                throw new IOException("Invalid GCP credentials JSON in application.properties: " + e.getMessage(), e);
            }
        } else {
            log.error("❌ No GCP credentials configured. Please set gcp.storage.credentials-json in application.properties");
            throw new IOException("GCP credentials not configured. Please set gcp.storage.credentials-json in application.properties");
        }

        try {
            this.storage = builder.build().getService();
            log.info("✅ GCP Storage Service initialized with bucket: {}", bucketName);

            // Ensure bucket exists or create it
            ensureBucketExists();
        } catch (Exception e) {
            log.error("❌ Failed to initialize GCP Storage Service: {}. Storage features may not work.", e.getMessage());
            throw new IOException("Failed to initialize GCP Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Ensure the bucket exists, create if it doesn't
     */
    private void ensureBucketExists() {
        try {
            Bucket bucket = storage.get(bucketName);
            if (bucket == null) {
                log.info("Bucket {} does not exist. Creating...", bucketName);
                BucketInfo bucketInfo = BucketInfo.newBuilder(bucketName)
                        .setLocation("ASIA-SOUTH1") // Mumbai region
                        .setStorageClass(StorageClass.STANDARD)
                        .build();
                storage.create(bucketInfo);
                log.info("Bucket {} created successfully", bucketName);
            } else {
                log.info("Bucket {} already exists", bucketName);
            }
        } catch (Exception e) {
            log.error("Error checking/creating bucket: {}", e.getMessage());
        }
    }

    /**
     * Generate folder name for vendor: businessName-vendorId
     * Example: "tcon-solutions-695380a5517d313aeb4c2e8c"
     */
    public String generateVendorFolderName(String vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new BadRequestException("Vendor not found"));

        String businessName = vendor.getStoreName() != null ?
                vendor.getStoreName() : "vendor";

        // Sanitize business name: lowercase, replace spaces with hyphens, remove special chars
        String sanitizedName = businessName.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        return sanitizedName + "-" + vendorId;
    }

    /**
     * Upload a product image for a vendor
     *
     * @param vendorId The vendor's MongoDB ID
     * @param file The image file to upload
     * @param productId Optional product ID for organizing images
     * @return The public URL of the uploaded image
     */
    public String uploadProductImage(String vendorId, MultipartFile file, String productId) {
        validateFile(file);

        String vendorFolder = generateVendorFolderName(vendorId);
        String fileName = generateUniqueFileName(file.getOriginalFilename());

        // Create blob path: vendors/{vendor-folder}/products/{productId}/{fileName}
        String blobPath = productId != null ?
                String.format("vendors/%s/products/%s/%s", vendorFolder, productId, fileName) :
                String.format("vendors/%s/products/%s", vendorFolder, fileName);

        return uploadFile(file, blobPath);
    }

    /**
     * Upload vendor logo
     */
    public String uploadVendorLogo(String vendorId, MultipartFile file) {
        validateFile(file);

        String vendorFolder = generateVendorFolderName(vendorId);
        String fileName = "logo-" + generateUniqueFileName(file.getOriginalFilename());

        String blobPath = String.format("vendors/%s/logo/%s", vendorFolder, fileName);

        return uploadFile(file, blobPath);
    }

    /**
     * Upload any file to GCP Storage
     */
    private String uploadFile(MultipartFile file, String blobPath) {
        try {
            BlobId blobId = BlobId.of(bucketName, blobPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .setAcl(Arrays.asList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))) // Make public
                    .build();

            Blob blob = storage.create(blobInfo, file.getBytes());

            String publicUrl = String.format("%s/%s/%s", baseUrl, bucketName, blobPath);

            log.info("File uploaded successfully: {}", publicUrl);
            return publicUrl;

        } catch (IOException e) {
            log.error("Error uploading file to GCP: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Upload multiple product images
     */
    public List<String> uploadProductImages(String vendorId, List<MultipartFile> files, String productId) {
        List<String> urls = new ArrayList<>();

        for (MultipartFile file : files) {
            String url = uploadProductImage(vendorId, file, productId);
            urls.add(url);
        }

        return urls;
    }

    /**
     * Delete a file from GCP Storage
     */
    public boolean deleteFile(String fileUrl) {
        try {
            // Extract blob path from URL
            String blobPath = extractBlobPathFromUrl(fileUrl);
            if (blobPath == null) {
                log.warn("Could not extract blob path from URL: {}", fileUrl);
                return false;
            }

            BlobId blobId = BlobId.of(bucketName, blobPath);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.info("File deleted successfully: {}", fileUrl);
            } else {
                log.warn("File not found or already deleted: {}", fileUrl);
            }

            return deleted;

        } catch (Exception e) {
            log.error("Error deleting file from GCP: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Delete multiple files
     */
    public void deleteFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }

        for (String url : fileUrls) {
            deleteFile(url);
        }
    }

    /**
     * Delete all files in a vendor's folder
     */
    public void deleteVendorFolder(String vendorId) {
        String vendorFolder = generateVendorFolderName(vendorId);
        String prefix = "vendors/" + vendorFolder + "/";

        try {
            Page<Blob> blobs = storage.list(bucketName,
                    Storage.BlobListOption.prefix(prefix));

            int deletedCount = 0;
            for (Blob blob : blobs.iterateAll()) {
                blob.delete();
                deletedCount++;
            }

            log.info("Deleted {} files from vendor folder: {}", deletedCount, vendorFolder);

        } catch (Exception e) {
            log.error("Error deleting vendor folder: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to delete vendor folder");
        }
    }

    /**
     * Generate a signed URL for temporary access (useful for private files)
     */
    public String generateSignedUrl(String blobPath, int durationMinutes) {
        try {
            BlobId blobId = BlobId.of(bucketName, blobPath);
            Blob blob = storage.get(blobId);

            if (blob == null) {
                throw new BadRequestException("File not found: " + blobPath);
            }

            return blob.signUrl(durationMinutes, TimeUnit.MINUTES).toString();

        } catch (Exception e) {
            log.error("Error generating signed URL: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to generate signed URL");
        }
    }

    /**
     * List all files in a vendor's folder
     */
    public List<String> listVendorFiles(String vendorId, String subFolder) {
        String vendorFolder = generateVendorFolderName(vendorId);
        String prefix = subFolder != null ?
                String.format("vendors/%s/%s/", vendorFolder, subFolder) :
                String.format("vendors/%s/", vendorFolder);

        List<String> fileUrls = new ArrayList<>();

        try {
            Page<Blob> blobs = storage.list(bucketName,
                    Storage.BlobListOption.prefix(prefix));

            for (Blob blob : blobs.iterateAll()) {
                String url = String.format("%s/%s/%s", baseUrl, bucketName, blob.getName());
                fileUrls.add(url);
            }

            return fileUrls;

        } catch (Exception e) {
            log.error("Error listing files: {}", e.getMessage(), e);
            throw new BadRequestException("Failed to list files");
        }
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        // Validate file size (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new BadRequestException("File size exceeds maximum limit of 5MB");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new BadRequestException("Invalid file type. Only images are allowed (JPEG, PNG, GIF, WEBP)");
        }
    }

    /**
     * Check if file is a valid image type
     */
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp") ||
               contentType.equals("image/jpg");
    }

    /**
     * Generate unique file name
     */
    private String generateUniqueFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis() + extension;
    }

    /**
     * Extract blob path from public URL
     */
    private String extractBlobPathFromUrl(String url) {
        try {
            // URL format: https://storage.googleapis.com/bucket-name/path/to/file
            String prefix = baseUrl + "/" + bucketName + "/";
            if (url.startsWith(prefix)) {
                return url.substring(prefix.length());
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting blob path from URL: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get file metadata
     */
    public Map<String, Object> getFileMetadata(String fileUrl) {
        String blobPath = extractBlobPathFromUrl(fileUrl);
        if (blobPath == null) {
            throw new BadRequestException("Invalid file URL");
        }

        BlobId blobId = BlobId.of(bucketName, blobPath);
        Blob blob = storage.get(blobId);

        if (blob == null) {
            throw new BadRequestException("File not found");
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", blob.getName());
        metadata.put("size", blob.getSize());
        metadata.put("contentType", blob.getContentType());
        metadata.put("created", blob.getCreateTimeOffsetDateTime());
        metadata.put("updated", blob.getUpdateTimeOffsetDateTime());
        metadata.put("url", fileUrl);

        return metadata;
    }
}

