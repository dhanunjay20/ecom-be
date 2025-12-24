package com.tcon.ecom.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferences {
    private Boolean orderUpdates = true;
    private Boolean promotions = true;
    private Boolean newsletter = false;
    private Boolean productUpdates = true;
}

