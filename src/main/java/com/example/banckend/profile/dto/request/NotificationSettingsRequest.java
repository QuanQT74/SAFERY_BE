package com.example.banckend.profile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsRequest {

    private Boolean pushEnabled;
    private Boolean soundEnabled;
    private Boolean dangerAlertsEnabled;
    private Boolean warningAlertsEnabled;
    private Boolean offlineAlertsEnabled;
    private Boolean fanStatusAlertsEnabled;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
}
