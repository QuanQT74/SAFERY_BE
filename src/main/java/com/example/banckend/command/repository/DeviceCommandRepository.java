package com.example.banckend.command.repository;

import com.example.banckend.command.entity.DeviceCommand;
import com.example.banckend.conmon.enums.CommandStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceCommandRepository extends JpaRepository<DeviceCommand, Long> {

    List<DeviceCommand> findByDeviceIdAndStatus(Long deviceId, CommandStatus status);

    Optional<DeviceCommand> findFirstByDeviceIdAndStatusOrderByCreatedAtAsc(Long deviceId, CommandStatus status);
}