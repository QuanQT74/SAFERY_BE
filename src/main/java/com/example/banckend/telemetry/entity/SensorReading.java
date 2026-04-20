package com.example.banckend.telemetry.entity;

import com.example.banckend.conmon.entity.BaseEntity;
import com.example.banckend.device.entity.Device;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(
    name = "sensor_readings",
    indexes = {
        @Index(name = "idx_sensor_reading_device", columnList = "device_id"),
        @Index(name = "idx_sensor_reading_recorded_at", columnList = "recordedAt"),
        @Index(name = "idx_sensor_reading_device_recorded_at", columnList = "device_id, recordedAt")
    }
)
public class SensorReading extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false)
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
    private Boolean autoMode = false;

    @Column(nullable = false)
    private LocalDateTime recordedAt;
}
