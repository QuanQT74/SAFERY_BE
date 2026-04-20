package com.example.banckend.device.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.example.banckend.auth.entity.User;
import com.example.banckend.conmon.entity.BaseEntity;
import com.example.banckend.conmon.enums.DeviceRole;

@Getter
@Setter
@Entity
@Table(
    name = "device_pairings",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_device_pairing", columnNames = {"user_id", "device_id"})
    }
)
public class DevicePairing extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceRole role = DeviceRole.OWNER;

    @Column(nullable = false)
    private LocalDateTime pairedAt;
}