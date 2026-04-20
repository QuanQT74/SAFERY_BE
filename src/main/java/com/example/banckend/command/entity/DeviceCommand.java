package com.example.banckend.command.entity;

import com.example.banckend.conmon.entity.BaseEntity;
import com.example.banckend.conmon.enums.CommandStatus;
import com.example.banckend.conmon.enums.CommandType;
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
@Table(name = "device_commands")
public class DeviceCommand extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CommandType commandType;

    @Column(length = 255)
    private String commandValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommandStatus status = CommandStatus.PENDING;

    private LocalDateTime executedAt;
}
