package com.example.banckend.config.websocket;

import com.example.banckend.command.dto.response.DeviceCommandResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CommandController {

    // Khi ESP32 gửi đến /app/commands, trả về danh sách lệnh pending
    @MessageMapping("/commands")
    @SendTo("/queue/commands")
    public List<DeviceCommandResponse> sendPendingCommands(List<DeviceCommandResponse> commands) {
        // Thực tế, chúng ta sẽ lấy từ service, nhưng ở đây chỉ echo lại
        return commands;
    }
}
