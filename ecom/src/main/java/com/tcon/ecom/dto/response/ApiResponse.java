package com.tcon.ecom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private ErrorDetails error;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        ErrorDetails error = new ErrorDetails("ERROR", message, null);
        return new ApiResponse<>(false, message, null, error, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String code, String message, Object details) {
        ErrorDetails error = new ErrorDetails(code, message, details);
        return new ApiResponse<>(false, message, null, error, LocalDateTime.now());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetails {
        private String code;
        private String message;
        private Object details;
    }
}

