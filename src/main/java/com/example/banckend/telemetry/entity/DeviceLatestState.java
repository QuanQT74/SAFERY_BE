package  com.example.banckend.telemetry.entity;

import com.example.banckend.conmon.entity.BaseEntity;
import com.example.banckend.conmon.enums.DeviceStatus;
import com.example.banckend.device.entity.Device;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "device_latest_state")
public class DeviceLatestState extends BaseEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false, unique = true)
    private Device device;

    @Column(nullable = false)
    private Double gasValue;

    @Column(nullable = false)
    private Double coValue;

    @Column(nullable = false)
    private Double temperature;

    @Column(nullable = false)
    private Double humidity;

    @Column(nullable = false)
    private Boolean flameDetected = false;

    @Column(nullable = false)
    private Boolean fanStatus = false;

    @Column(nullable = false)
    private Boolean alarmArmed = false;

    @Column(nullable = false)
    private Boolean aiDetectedFire = false;

    // Trạng thái đang chờ xử lý (để App không bị giật nút)
    @Column(name = "pending_fan_status")
    private Boolean pendingFanStatus;

    @Column(name = "pending_alarm_status")
    private Boolean pendingAlarmStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceStatus overallStatus = DeviceStatus.OFFLINE;

    @Column(nullable = false)
    private LocalDateTime stateUpdatedAt;
}