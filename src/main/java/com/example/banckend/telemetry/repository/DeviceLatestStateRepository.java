package com.example.banckend.telemetry.repository;

import com.example.banckend.telemetry.entity.DeviceLatestState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceLatestStateRepository extends JpaRepository<DeviceLatestState, Long> {

    Optional<DeviceLatestState> findByDeviceId(Long deviceId);
}
