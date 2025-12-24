package com.tcon.ecom.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistory {
    private String status;
    private LocalDateTime timestamp;
    private String note;
    private String updatedBy; // userId or 'system'
}

