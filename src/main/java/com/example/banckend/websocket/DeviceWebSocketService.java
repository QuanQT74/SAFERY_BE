package com.example.banckend.websocket;

import com.example.banckend.command.dto.response.DeviceCommandResponse;
import com.example.banckend.command.entity.DeviceCommand;
import com.example.banckend.command.mapper.DeviceCommandMapper;
import com.example.banckend.command.repository.DeviceCommandRepository;
import com.example.banckend.conmon.enums.CommandStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final DeviceCommandRepository commandRepository;
    private final DeviceCommandMapper commandMapper;

    /**
     * Gọi hàm này ngay khi App tạo lệnh mới (fan/alarm).
     * Backend sẽ PUSH lệnh ngay lập tức xuống ESP32 qua WebSocket.
     * Chủ đề: /topic/device/{deviceCode}/commands
     */
    public void pushCommandToDevice(String deviceCode, DeviceCommandResponse command) {
        String destination = "/topic/device/" + deviceCode + "/commands";
        messagingTemplate.convertAndSend(destination, command);
        log.info("[WS] Pushed command {} to device {} via {}", command.getCommandType(), deviceCode, destination);
    }

    /**
     * Khi Backend nhận dữ liệu telemetry mới từ ESP32,
     * push dữ liệu mới nhất tới App đang lắng nghe.
     * Chủ đề: /topic/device/{deviceCode}/telemetry
     */
    public void pushTelemetryToApp(String deviceCode, Object telemetryData) {
        String destination = "/topic/device/" + deviceCode + "/telemetry";
        messagingTemplate.convertAndSend(destination, telemetryData);
        log.info("[WS] Pushed telemetry update for device {} to App", deviceCode);
    }

    /**
     * Khi có command (bật/tắt fan, alarm), push dữ liệu TRẠNG THÁI mới nhất cho App NGAY LẬP TỨC.
     * Đây là điểm quan trọng để App cập nhật UI tức thì mà KHÔNG cần chờ ESP32 gửi telemetry.
     * Chủ đề: /topic/device/{deviceCode}/state
     */
    public void pushLatestStateToApp(String deviceCode, Object stateData) {
        String destination = "/topic/device/" + deviceCode + "/state";
        messagingTemplate.convertAndSend(destination, stateData);
        log.info("[WS] Pushed latest state for device {} to App", deviceCode);
    }

    /**
     * Khi phát hiện DANGER, push alert tới App.
     * Chủ đề: /topic/device/{deviceCode}/alert
     */
    public void pushAlertToApp(String deviceCode, Object alertData) {
        String destination = "/topic/device/" + deviceCode + "/alert";
        messagingTemplate.convertAndSend(destination, alertData);
        log.info("[WS] Pushed DANGER alert for device {} to App", deviceCode);
    }

    /**
     * Khi ESP32 kết nối qua WebSocket, lấy lệnh đang PENDING và PUSH xuống ngay.
     */
    public void pushPendingCommandsOnConnect(Long deviceId) {
        List<DeviceCommand> pending = commandRepository
                .findByDeviceIdAndStatus(deviceId, CommandStatus.PENDING);

        if (!pending.isEmpty()) {
            // Lấy deviceCode từ một trong các command (cùng device)
            String deviceCode = pending.get(0).getDevice().getDeviceCode();

            pending.forEach(cmd -> {
                cmd.setStatus(CommandStatus.SENT);
                cmd.setExecutedAt(LocalDateTime.now());
            });
            commandRepository.saveAll(pending);

            List<DeviceCommandResponse> responses = commandMapper.toResponseList(pending);
            String destination = "/topic/device/" + deviceCode + "/commands";
            messagingTemplate.convertAndSend(destination, responses);
            log.info("[WS] Pushed {} pending commands to device {} on connect", pending.size(), deviceCode);
        }
    }
}
