package com.example.banckend.profile.mapper;

import com.example.banckend.profile.dto.request.NotificationSettingsRequest;
import com.example.banckend.profile.dto.response.NotificationSettingsResponse;
import com.example.banckend.profile.entity.NotificationSettings;
import com.example.banckend.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class NotificationSettingsMapper {

    public NotificationSettings toEntity(User user, NotificationSettingsRequest request) {
        NotificationSettings settings = new NotificationSettings();
        settings.setUser(user);
        settings.setPushEnabled(request.getPushEnabled());
        settings.setSoundEnabled(request.getSoundEnabled());
        settings.setDangerAlertsEnabled(request.getDangerAlertsEnabled());
        settings.setWarningAlertsEnabled(request.getWarningAlertsEnabled());
        settings.setOfflineAlertsEnabled(request.getOfflineAlertsEnabled());
        settings.setFanStatusAlertsEnabled(request.getFanStatusAlertsEnabled());
        settings.setQuietHoursStart(request.getQuietHoursStart());
        settings.setQuietHoursEnd(request.getQuietHoursEnd());
        return settings;
    }

    public NotificationSettingsResponse toResponse(NotificationSettings settings) {
        return NotificationSettingsResponse.builder()
                .id(settings.getId())
                .pushEnabled(settings.getPushEnabled())
                .soundEnabled(settings.getSoundEnabled())
                .dangerAlertsEnabled(settings.getDangerAlertsEnabled())
                .warningAlertsEnabled(settings.getWarningAlertsEnabled())
                .offlineAlertsEnabled(settings.getOfflineAlertsEnabled())
                .fanStatusAlertsEnabled(settings.getFanStatusAlertsEnabled())
                .quietHoursStart(settings.getQuietHoursStart())
                .quietHoursEnd(settings.getQuietHoursEnd())
                .build();
    }

    public void updateEntity(NotificationSettings settings, NotificationSettingsRequest request) {
        settings.setPushEnabled(request.getPushEnabled());
        settings.setSoundEnabled(request.getSoundEnabled());
        settings.setDangerAlertsEnabled(request.getDangerAlertsEnabled());
        settings.setWarningAlertsEnabled(request.getWarningAlertsEnabled());
        settings.setOfflineAlertsEnabled(request.getOfflineAlertsEnabled());
        settings.setFanStatusAlertsEnabled(request.getFanStatusAlertsEnabled());
        settings.setQuietHoursStart(request.getQuietHoursStart());
        settings.setQuietHoursEnd(request.getQuietHoursEnd());
    }
}
