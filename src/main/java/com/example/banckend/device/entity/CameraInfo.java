package com.example.banckend.device.entity;


import com.example.banckend.conmon.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "camera_info")
public class CameraInfo extends BaseEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false, unique = true)
    private Device device;

    @Column(length = 500)
    private String streamUrl;

    @Column(length = 500)
    private String snapshotUrl;

    @Column(nullable = false)
    private Boolean online = false;

    private LocalDateTime lastUpdatedAt;
}