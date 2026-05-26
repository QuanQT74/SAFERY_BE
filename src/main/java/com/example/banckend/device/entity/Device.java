package com.example.banckend.device.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.example.banckend.conmon.entity.BaseEntity;
import com.example.banckend.conmon.enums.DeviceStatus;
import com.example.banckend.telemetry.entity.DeviceLatestState;

@Getter
@Setter
@Entity
@Table(name = "devices")
public class Device extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String deviceCode;

    @Column(nullable = false, length = 150)
    private String deviceName;

    @Column(length = 150)
    private String nickname;

    @Column(nullable = false, unique = true, length = 100)
    private String esp32Serial;

    @Column(unique = true, length = 100)
    private String hardwareId;

    @Column(unique = true, length = 50)
    private String deviceSecret;

    @Column(length = 50)
    private String firmwareVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceStatus status = DeviceStatus.OFFLINE;

    @Column(nullable = false)
    private Boolean online = false;

    private LocalDateTime lastSeenAt;

    @OneToOne(mappedBy = "device")
    private CameraInfo cameraInfo;

    @OneToOne(mappedBy = "device")
    private SensorThreshold sensorThreshold;

    @OneToOne(mappedBy = "device")
    private DeviceLatestState latestState;

    @Column(name = "owner_user_id")
    private Long ownerUserId;
}