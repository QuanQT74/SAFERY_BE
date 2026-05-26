package com.example.banckend.alert.repository;

import com.example.banckend.alert.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.banckend.conmon.enums.AlertSeverity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {


    


    @Query("SELECT a FROM Alert a WHERE a.device.ownerUserId = :userId " +
            "AND (:severity IS NULL OR a.severity = :severity) " +
            "AND (:acknowledged IS NULL OR a.acknowledged = :acknowledged) " +
            "ORDER BY a.createdAtEvent DESC")
    Page<Alert> findFilteredAlerts(@Param("userId") Long userId,
            @Param("severity") AlertSeverity severity,
            @Param("acknowledged") Boolean acknowledged,
            Pageable pageable);

    List<Alert> findByDeviceOwnerUserIdOrderByCreatedAtEventDesc(Long userId);

    Page<Alert> findByDeviceOwnerUserIdOrderByCreatedAtEventDesc(Long userId, Pageable pageable);

    List<Alert> findByDeviceIdAndAcknowledgedFalseOrderByCreatedAtEventDesc(Long deviceId);

    long countByDeviceIdAndAcknowledgedFalse(Long deviceId);

    long countByDeviceOwnerUserIdAndAcknowledgedFalse(Long userId);

    boolean existsByDeviceIdAndTypeAndAcknowledgedFalse(Long deviceId,
            com.example.banckend.conmon.enums.AlertType type);

    List<Alert> findByDeviceOwnerUserIdAndAcknowledgedFalseOrderByCreatedAtEventDesc(Long userId);

    long countByDeviceOwnerUserId(Long userId);

    java.util.Optional<Alert> findFirstByDeviceIdAndTypeAndAcknowledgedFalseOrderByCreatedAtEventDesc(Long deviceId,
            com.example.banckend.conmon.enums.AlertType type);
}