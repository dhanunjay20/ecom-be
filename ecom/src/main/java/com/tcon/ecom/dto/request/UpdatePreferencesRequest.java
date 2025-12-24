package com.tcon.ecom.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferencesRequest {
    private String language;
    private String currency;
    private NotificationPreferences notifications;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationPreferences {
        private Boolean orderUpdates;
        private Boolean promotions;
        private Boolean newsletter;
        private Boolean productUpdates;
    }
}

