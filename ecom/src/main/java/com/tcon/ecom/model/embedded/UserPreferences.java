package com.tcon.ecom.model.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {
    private String language = "en";
    private String currency = "USD";
    private NotificationPreferences notifications = new NotificationPreferences();
}

