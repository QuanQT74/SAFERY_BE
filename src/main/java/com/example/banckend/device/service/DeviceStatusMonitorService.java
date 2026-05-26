package com.example.banckend.device.service;

import com.example.banckend.conmon.enums.DeviceStatus;
import com.example.banckend.device.entity.Device;
import com.example.banckend.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceStatusMonitorService {

    private final DeviceRepository deviceRepository;

    @Value("${device.offline.timeout.minutes:5}")
    private int offlineTimeoutMinutes;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkDeviceStatus() {
        log.debug("Checking device status for offline devices...");
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(offlineTimeoutMinutes);

        List<Device> onlineDevices = deviceRepository.findByOnlineTrue();
        int updatedCount = 0;

        for (Device device : onlineDevices) {
            if (device.getLastSeenAt() != null && device.getLastSeenAt().isBefore(timeoutThreshold)) {
                device.setOnline(false);
                device.setStatus(DeviceStatus.OFFLINE);
                deviceRepository.save(device);
                updatedCount++;
                log.info("Device {} marked as OFFLINE (no telemetry for {} minutes)",
                        device.getDeviceCode(), offlineTimeoutMinutes);
            }
        }

        if (updatedCount > 0) {
            log.info("Marked {} devices as OFFLINE", updatedCount);
        }
    }
}