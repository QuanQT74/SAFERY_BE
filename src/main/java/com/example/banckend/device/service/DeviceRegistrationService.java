package com.example.banckend.device.service;

import com.example.banckend.command.service.DeviceCommandService;
import com.example.banckend.conmon.enums.DeviceErrorCode;
import com.example.banckend.conmon.enums.DeviceStatus;
import com.example.banckend.conmon.exception.ResourceNotFoundException;
import com.example.banckend.device.dto.request.PairDeviceRequest;
import com.example.banckend.device.dto.request.RegisterDeviceRequest;
import com.example.banckend.device.dto.response.DeviceResponse;
import com.example.banckend.device.dto.response.DeviceTelemetryResponse;
import com.example.banckend.device.dto.response.LatestReadingResponse;
import com.example.banckend.device.dto.response.PairDeviceResponse;
import com.example.banckend.device.dto.response.RegisterDeviceResponse;
import com.example.banckend.device.entity.Device;
import com.example.banckend.device.mapper.DeviceMapper;
import com.example.banckend.device.repository.DeviceRepository;
import com.example.banckend.telemetry.entity.DeviceLatestState;
import com.example.banckend.telemetry.repository.DeviceLatestStateRepository;
import com.example.banckend.device.repository.DevicePairingRepository;
import com.example.banckend.device.entity.DevicePairing;
import com.example.banckend.auth.repository.UserRepository;
import com.example.banckend.auth.entity.User;
import com.example.banckend.conmon.enums.DeviceRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceRegistrationService {

    private final DeviceCommandService deviceCommandService;
    private final DeviceRepository deviceRepository;
    private final DeviceLatestStateRepository deviceLatestStateRepository;
    private final DeviceMapper deviceMapper;
    private final DevicePairingRepository devicePairingRepository;
    private final UserRepository userRepository;

    private static final String DEVICE_CODE_PREFIX = "ESP32_FIRE_";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public PairDeviceResponse pairAndProvision(PairDeviceRequest request, Long userId) {
        Optional<Device> existingDevice = deviceRepository.findByHardwareIdAndOwnerUserId(
                request.getHardwareId(), userId);

        if (existingDevice.isPresent()) {
            Device device = existingDevice.get();
            return PairDeviceResponse.builder()
                    .deviceCode(device.getDeviceCode())
                    .deviceSecret(device.getDeviceSecret())
                    .deviceName(device.getDeviceName())
                    .nickname(device.getNickname())
                    .build();
        }

        Optional<Device> existingByHardwareId = deviceRepository.findByHardwareId(request.getHardwareId());
        if (existingByHardwareId.isPresent()) {
            Device device = existingByHardwareId.get();
            if (device.getOwnerUserId() != null) {
                throw new ResourceNotFoundException(DeviceErrorCode.DEVICE_ALREADY_PAIRED);
            }
            if (device.getStatus() != DeviceStatus.PAIRING) {
                throw new ResourceNotFoundException(DeviceErrorCode.BAD_REQUEST);
            }
            device.setOwnerUserId(userId);
            deviceRepository.save(device);

            // Tạo bản ghi DevicePairing
            if (!devicePairingRepository.existsByDeviceIdAndUserId(device.getId(), userId)) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.UNAUTHORIZED));
                DevicePairing pairing = new DevicePairing();
                pairing.setUser(user);
                pairing.setDevice(device);
                pairing.setRole(DeviceRole.OWNER);
                pairing.setPairedAt(LocalDateTime.now());
                devicePairingRepository.save(pairing);
            }

            return PairDeviceResponse.builder()
                    .deviceCode(device.getDeviceCode())
                    .deviceSecret(device.getDeviceSecret())
                    .deviceName(device.getDeviceName())
                    .nickname(device.getNickname())
                    .build();
        }
        String deviceCode = generateDeviceCode();
        String deviceSecret = generateDeviceSecret();

        Device device = new Device();
        device.setHardwareId(request.getHardwareId());
        device.setDeviceCode(deviceCode);
        device.setDeviceSecret(deviceSecret);
        device.setDeviceName(request.getDeviceName());
        device.setNickname(request.getNickname());
        device.setEsp32Serial(request.getEsp32Serial());
        device.setFirmwareVersion(request.getFirmwareVersion());
        device.setStatus(DeviceStatus.OFFLINE);
        device.setOnline(false);
        device.setOwnerUserId(userId);

        deviceRepository.save(device);

        // Tạo bản ghi DevicePairing
        boolean pairingExists = devicePairingRepository
                .existsByDeviceIdAndUserId(device.getId(), userId);

        if (!pairingExists) {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.UNAUTHORIZED));

            DevicePairing pairing = new DevicePairing();

            pairing.setUser(user);
            pairing.setDevice(device);
            pairing.setRole(DeviceRole.OWNER);
            pairing.setPairedAt(LocalDateTime.now());

            devicePairingRepository.save(pairing);
        }

        DeviceLatestState latestState = new DeviceLatestState();
        latestState.setDevice(device);
        latestState.setGasValue(0.0);
        latestState.setCoValue(0.0);
        latestState.setTemperature(0.0);
        latestState.setHumidity(0.0);
        latestState.setFlameDetected(false);
        latestState.setFanStatus(false);
        latestState.setAlarmArmed(false);
        latestState.setAiDetectedFire(false);
        latestState.setOverallStatus(DeviceStatus.OFFLINE);
        latestState.setStateUpdatedAt(LocalDateTime.now());

        deviceLatestStateRepository.save(latestState);

        return PairDeviceResponse.builder()
                .deviceCode(deviceCode)
                .deviceSecret(deviceSecret)
                .deviceName(request.getDeviceName())
                .nickname(request.getNickname())
                .build();
    }

    @Transactional
    public RegisterDeviceResponse registerDevice(RegisterDeviceRequest request) {
        Optional<Device> existingDevice = deviceRepository.findByHardwareId(request.getHardwareId());

        if (existingDevice.isPresent()) {
            Device device = existingDevice.get();
            return RegisterDeviceResponse.builder()
                    .deviceCode(device.getDeviceCode())
                    .deviceSecret(device.getDeviceSecret())
                    .deviceName(device.getDeviceName())
                    .build();
        }

        String deviceCode = generateDeviceCode();
        String deviceSecret = generateDeviceSecret();

        Device device = new Device();
        device.setHardwareId(request.getHardwareId());
        device.setDeviceCode(deviceCode);
        device.setDeviceSecret(deviceSecret);
        device.setDeviceName(request.getDeviceName());
        device.setNickname(request.getNickname());
        device.setEsp32Serial(request.getEsp32Serial());
        device.setFirmwareVersion(request.getFirmwareVersion());
        device.setStatus(DeviceStatus.OFFLINE);
        device.setOnline(false);

        deviceRepository.save(device);

        DeviceLatestState latestState = new DeviceLatestState();
        latestState.setDevice(device);
        latestState.setGasValue(0.0);
        latestState.setCoValue(0.0);
        latestState.setTemperature(0.0);
        latestState.setHumidity(0.0);
        latestState.setFlameDetected(false);
        latestState.setFanStatus(false);
        latestState.setAlarmArmed(false);
        latestState.setAiDetectedFire(false);
        latestState.setOverallStatus(DeviceStatus.OFFLINE);
        latestState.setStateUpdatedAt(LocalDateTime.now());

        deviceLatestStateRepository.save(latestState);

        return RegisterDeviceResponse.builder()
                .deviceCode(deviceCode)
                .deviceSecret(deviceSecret)
                .deviceName(request.getDeviceName())
                .build();
    }

    @Transactional(readOnly = true)
    public LatestReadingResponse getLatestReading(String deviceCode, Long userId) {
        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.DEVICE_NOT_FOUND));

        if (device.getOwnerUserId() == null || !device.getOwnerUserId().equals(userId)) {
            throw new ResourceNotFoundException(DeviceErrorCode.DEVICE_NOT_FOUND);
        }

        DeviceLatestState latestState = deviceLatestStateRepository.findByDeviceId(device.getId())
                .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.RESOURCE_NOT_FOUND));

        return deviceMapper.toLatestReadingResponse(device, latestState);
    }

    @Transactional(readOnly = true)
    public DeviceTelemetryResponse getTelemetryResponse(String deviceCode, Long userId) {
        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.DEVICE_NOT_FOUND));

        if (device.getOwnerUserId() == null || !device.getOwnerUserId().equals(userId)) {
            throw new ResourceNotFoundException(DeviceErrorCode.DEVICE_NOT_FOUND);
        }

        DeviceLatestState latestState = deviceLatestStateRepository.findByDeviceId(device.getId())
                .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.RESOURCE_NOT_FOUND));

        return deviceMapper.toTelemetryResponse(device, latestState);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevicesByUserId(Long userId) {
        List<Device> devices = deviceRepository.findByOwnerUserId(userId);
        return devices.stream()
                .map(deviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    private String generateDeviceCode() {
        long count = deviceRepository.count();
        return DEVICE_CODE_PREFIX + String.format("%03d", count + 1);
    }

    private String generateDeviceSecret() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder("sec_");
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Transactional
    public void DisconnectDevice(String deviceCode, Long userId) {
        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.DEVICE_NOT_FOUND));

        if (device.getOwnerUserId() == null || !device.getOwnerUserId().equals(userId)) {
            throw new ResourceNotFoundException(DeviceErrorCode.UNAUTHORIZED);
        }
        try {
            deviceCommandService.createDisconnectCommand(deviceCode, userId);
        } catch (Exception e) {
            log.warn("Thiết bị {} offline, không thể gửi lệnh reset trực tiếp. Tiếp tục xử lý trên Server.",
                    deviceCode);
        }

        // Reset Device Ownership
        device.setDeviceSecret(generateDeviceSecret());
        device.setOwnerUserId(null);
        device.setOnline(false);
        device.setNickname(null);
        device.setLastSeenAt(null);
        device.setStatus(DeviceStatus.PAIRING);
        deviceRepository.save(device);

        // Xóa DevicePairing (Thay vì lưu mới như lỗi trước đó)
        devicePairingRepository.deleteByDeviceIdAndUserId(device.getId(), userId);

        // Reset state
        deviceLatestStateRepository.findByDeviceId(device.getId()).ifPresent(state -> {
            state.setGasValue(0.0);
            state.setCoValue(0.0);
            state.setTemperature(0.0);
            state.setHumidity(0.0);
            state.setFlameDetected(false);
            state.setFanStatus(false);
            state.setAlarmArmed(false);
            state.setAiDetectedFire(false);
            state.setOverallStatus(DeviceStatus.OFFLINE);
            state.setStateUpdatedAt(LocalDateTime.now());
            deviceLatestStateRepository.save(state);
        });
        log.info("Thiết bị {} đã được ngắt kết nối bởi user {}", deviceCode, userId);
    }

    @Transactional
    public void updateNickname(String deviceCode, Long userId, String nickname) {
        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.DEVICE_NOT_FOUND));

        if (device.getOwnerUserId() == null || !device.getOwnerUserId().equals(userId)) {
            throw new ResourceNotFoundException(DeviceErrorCode.UNAUTHORIZED);
        }

        device.setNickname(nickname);
        deviceRepository.save(device);
        log.info("Nickname updated for device {} by user {}", deviceCode, userId);
    }
    
    @Transactional
    public void setDeviceToPairingMode(String HardwareId) {
        Device device = deviceRepository.findByHardwareId(HardwareId)
                .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.DEVICE_NOT_FOUND));

        if (device.getOwnerUserId() == null) {
            device.setStatus(DeviceStatus.PAIRING);
            deviceRepository.save(device);
            log.info("Thiết bị {} đã chuyển sang chế độ PAIRING", HardwareId);
        } else {
            log.warn("Thiết bị {} đang có chủ, không thể tự ý vào chế độ Pairing", HardwareId);
        }
    }
}
