
package com.example.banckend.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlertPushDto {
    private String title;
    private String message;
    private String type; // Ví dụ: "DANGER", "WARNING", "OFFLINE"
    private String deviceCode;
    private Map<String, String> extraData; // key/value tuỳ ý (ví dụ: alertId)
}
