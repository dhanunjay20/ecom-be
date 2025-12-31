package com.tcon.ecom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiFileUploadResponse {
    private List<String> fileUrls;
    private Integer uploadedCount;
    private Integer totalCount;
    private String vendorFolder;
    private String message;
}

