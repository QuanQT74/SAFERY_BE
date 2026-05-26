package com.example.banckend.device.repository;

import com.example.banckend.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByHardwareId(String hardwareId);
    Optional<Device> findByDeviceCode(String deviceCode);
    Optional<Device> findByHardwareIdAndOwnerUserId(String hardwareId, Long ownerUserId);
    List<Device> findByOnlineTrue();
    List<Device> findByOwnerUserId(Long ownerUserId);
}
