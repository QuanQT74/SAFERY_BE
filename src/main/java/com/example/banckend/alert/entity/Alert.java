package com.example.banckend.alert.entity;

import com.example.banckend.conmon.entity.BaseEntity;
import com.example.banckend.conmon.enums.AlertSeverity;
import com.example.banckend.conmon.enums.AlertType;
import com.example.banckend.device.entity.Device;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "alerts")
public class Alert extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    private Double sensorValue;

    @Column(length = 20)
    private String unit;

    @Column(nullable = false)
    private Boolean acknowledged = false;

    @Column(nullable = false)
    private LocalDateTime createdAtEvent;

    private LocalDateTime resolvedAt;
}
