package com.tcon.ecom.controller;

import com.tcon.ecom.dto.response.ApiResponse;
import com.tcon.ecom.dto.response.FileUploadResponse;
import com.tcon.ecom.dto.response.MultiFileUploadResponse;
import com.tcon.ecom.service.GcpStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Tag(name = "Storage", description = "File upload and management APIs")
@Slf4j
@ConditionalOnProperty(name = "gcp.storage.enabled", havingValue = "true", matchIfMissing = false)
public class StorageController {

    private final GcpStorageService gcpStorageService;

    @PostMapping(value = "/upload/product-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(
            summary = "Upload product image",
            description = "Upload a single product image to GCP storage. Creates vendor-specific folder.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<FileUploadResponse> uploadProductImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("vendorId") String vendorId,
            @RequestParam(value = "productId", required = false) String productId) {

        log.info("Uploading product image for vendor: {}, product: {}", vendorId, productId);

        String fileUrl = gcpStorageService.uploadProductImage(vendorId, file, productId);
        String vendorFolder = gcpStorageService.generateVendorFolderName(vendorId);

        FileUploadResponse response = FileUploadResponse.builder()
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .vendorFolder(vendorFolder)
                .message("File uploaded successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload/product-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(
            summary = "Upload multiple product images",
            description = "Upload multiple product images at once",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<MultiFileUploadResponse> uploadProductImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("vendorId") String vendorId,
            @RequestParam(value = "productId", required = false) String productId) {

        log.info("Uploading {} product images for vendor: {}, product: {}", files.size(), vendorId, productId);

        List<String> fileUrls = gcpStorageService.uploadProductImages(vendorId, files, productId);
        String vendorFolder = gcpStorageService.generateVendorFolderName(vendorId);

        MultiFileUploadResponse response = MultiFileUploadResponse.builder()
                .fileUrls(fileUrls)
                .uploadedCount(fileUrls.size())
                .totalCount(files.size())
                .vendorFolder(vendorFolder)
                .message(fileUrls.size() + " files uploaded successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload/vendor-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(
            summary = "Upload vendor logo",
            description = "Upload vendor logo/profile image",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<FileUploadResponse> uploadVendorLogo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("vendorId") String vendorId) {

        log.info("Uploading vendor logo for vendor: {}", vendorId);

        String fileUrl = gcpStorageService.uploadVendorLogo(vendorId, file);
        String vendorFolder = gcpStorageService.generateVendorFolderName(vendorId);

        FileUploadResponse response = FileUploadResponse.builder()
                .fileUrl(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .vendorFolder(vendorFolder)
                .message("Vendor logo uploaded successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    @Operation(
            summary = "Delete file",
            description = "Delete a file from GCP storage",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        log.info("Deleting file: {}", fileUrl);

        boolean deleted = gcpStorageService.deleteFile(fileUrl);

        if (deleted) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("File deleted successfully")
                    .build());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("File not found or already deleted")
                            .build());
        }
    }

    @DeleteMapping("/delete/multiple")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    @Operation(
            summary = "Delete multiple files",
            description = "Delete multiple files from GCP storage",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse> deleteFiles(@RequestBody List<String> fileUrls) {
        log.info("Deleting {} files", fileUrls.size());

        gcpStorageService.deleteFiles(fileUrls);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(fileUrls.size() + " files deleted successfully")
                .build());
    }

    @DeleteMapping("/vendor-folder/{vendorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete vendor folder",
            description = "Delete all files in a vendor's folder (Admin only)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse> deleteVendorFolder(@PathVariable String vendorId) {
        log.info("Deleting vendor folder for vendor: {}", vendorId);

        gcpStorageService.deleteVendorFolder(vendorId);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Vendor folder deleted successfully")
                .build());
    }

    @GetMapping("/list/vendor-files")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    @Operation(
            summary = "List vendor files",
            description = "List all files in a vendor's folder",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<String>> listVendorFiles(
            @RequestParam("vendorId") String vendorId,
            @RequestParam(value = "subFolder", required = false) String subFolder) {

        log.info("Listing files for vendor: {}, subFolder: {}", vendorId, subFolder);

        List<String> fileUrls = gcpStorageService.listVendorFiles(vendorId, subFolder);

        return ResponseEntity.ok(fileUrls);
    }

    @GetMapping("/metadata")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    @Operation(
            summary = "Get file metadata",
            description = "Get metadata information for a file",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Map<String, Object>> getFileMetadata(@RequestParam("fileUrl") String fileUrl) {
        log.info("Getting metadata for file: {}", fileUrl);

        Map<String, Object> metadata = gcpStorageService.getFileMetadata(fileUrl);

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/vendor-folder/{vendorId}")
    @PreAuthorize("hasRole('VENDOR') or hasRole('ADMIN')")
    @Operation(
            summary = "Get vendor folder name",
            description = "Get the folder name for a vendor",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Map<String, String>> getVendorFolderName(@PathVariable String vendorId) {
        String folderName = gcpStorageService.generateVendorFolderName(vendorId);

        return ResponseEntity.ok(Map.of(
                "vendorId", vendorId,
                "folderName", folderName,
                "folderPath", "vendors/" + folderName
        ));
    }
}

