package com.example.banckend.device.repository;

import com.example.banckend.device.entity.DevicePairing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DevicePairingRepository extends JpaRepository<DevicePairing, Long> {
    Optional<DevicePairing> findByDeviceIdAndUserId(Long deviceId, Long userId);
    void deleteByDeviceIdAndUserId(Long deviceId, Long userId);

    boolean existsByDeviceIdAndUserId(Long deviceId, Long userId);

}
