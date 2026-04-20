package com.example.banckend.device.entity;


import com.example.banckend.conmon.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sensor_thresholds")
public class SensorThreshold extends BaseEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false, unique = true)
    private Device device;

    @Column(nullable = false)
    private Double gasWarningThreshold;

    @Column(nullable = false)
    private Double gasDangerThreshold;

    @Column(nullable = false)
    private Double coWarningThreshold;

    @Column(nullable = false)
    private Double coDangerThreshold;

    @Column(nullable = false)
    private Double temperatureWarningThreshold;

    @Column(nullable = false)
    private Double temperatureDangerThreshold;
}