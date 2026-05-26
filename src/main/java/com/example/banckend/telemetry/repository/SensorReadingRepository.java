package com.example.banckend.telemetry.repository;

import com.example.banckend.telemetry.entity.SensorReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    // Lấy lịch sử theo deviceId + khoảng thời gian, sắp xếp mới nhất trước
    @Query("SELECT s FROM SensorReading s WHERE s.device.id = :deviceId " +
           "AND s.recordedAt BETWEEN :startTime AND :endTime " +
           "ORDER BY s.recordedAt DESC")
    Page<SensorReading> findByDeviceIdAndTimeRange(
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    // Lấy N bản ghi mới nhất theo deviceId
    @Query("SELECT s FROM SensorReading s WHERE s.device.id = :deviceId " +
           "ORDER BY s.recordedAt DESC")
    List<SensorReading> findLatestByDeviceId(@Param("deviceId") Long deviceId, Pageable pageable);

    // Tính toán thống kê Min, Max, Avg trong khoảng thời gian
    @Query("SELECT MAX(s.gasValue), MIN(s.gasValue), AVG(s.gasValue), " +
           "MAX(s.coValue), MIN(s.coValue), AVG(s.coValue), " +
           "MAX(s.temperature), MIN(s.temperature), AVG(s.temperature), " +
           "MAX(s.humidity), MIN(s.humidity), AVG(s.humidity) " +
           "FROM SensorReading s WHERE s.device.id = :deviceId " +
           "AND s.recordedAt BETWEEN :startTime AND :endTime")
    List<Object[]> findStatsByDeviceIdAndTimeRange(
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // Lấy dữ liệu Aggregated theo giờ (cho 24h)
    @Query(value = "SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d %H:00:00') as time_bucket, " +
                   "MAX(gas_value) as maxGas, MIN(gas_value) as minGas, AVG(gas_value) as avgGas, " +
                   "MAX(co_value) as maxCo, MIN(co_value) as minCo, AVG(co_value) as avgCo, " +
                   "MAX(temperature) as maxTemp, MIN(temperature) as minTemp, AVG(temperature) as avgTemp, " +
                   "MAX(humidity) as maxHum, MIN(humidity) as minHum, AVG(humidity) as avgHum " +
                   "FROM sensor_readings " +
                   "WHERE device_id = :deviceId AND recorded_at BETWEEN :startTime AND :endTime " +
                   "GROUP BY DATE_FORMAT(recorded_at, '%Y-%m-%d %H:00:00') " +
                   "ORDER BY time_bucket ASC", nativeQuery = true)
    List<Object[]> findHourlyAggregatedData(
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // Lấy dữ liệu Aggregated theo ngày (cho 7d, 30d)
    @Query(value = "SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d 00:00:00') as time_bucket, " +
                   "MAX(gas_value) as maxGas, MIN(gas_value) as minGas, AVG(gas_value) as avgGas, " +
                   "MAX(co_value) as maxCo, MIN(co_value) as minCo, AVG(co_value) as avgCo, " +
                   "MAX(temperature) as maxTemp, MIN(temperature) as minTemp, AVG(temperature) as avgTemp, " +
                   "MAX(humidity) as maxHum, MIN(humidity) as minHum, AVG(humidity) as avgHum " +
                   "FROM sensor_readings " +
                   "WHERE device_id = :deviceId AND recorded_at BETWEEN :startTime AND :endTime " +
                   "GROUP BY DATE_FORMAT(recorded_at, '%Y-%m-%d 00:00:00') " +
                   "ORDER BY time_bucket ASC", nativeQuery = true)
    List<Object[]> findDailyAggregatedData(
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // Kiểm tra xem có cảnh báo nào trong khoảng thời gian không
    @Query("SELECT COUNT(s) FROM SensorReading s WHERE s.device.id = :deviceId " +
           "AND s.recordedAt BETWEEN :startTime AND :endTime " +
           "AND (s.gasValue > 500 OR s.coValue > 20 OR s.temperature > 40 OR s.flameDetected = true OR s.aiDetectedFire = true)")
    Long countWarningsInPeriod(
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // ========== Per-sensor Aggregated Queries ==========

    // CO - Hourly
    @Query(value = "SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d %H:00:00') AS bucket, " +
               "MAX(co_value) AS maxVal, MIN(co_value) AS minVal, AVG(co_value) AS avgVal " +
               "FROM sensor_readings " +
               "WHERE device_id = :deviceId AND recorded_at BETWEEN :startTime AND :endTime " +
               "GROUP BY bucket ORDER BY bucket ASC", nativeQuery = true)
    List<Object[]> findHourlyCo(@Param("deviceId") Long deviceId,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);

    // CO - Daily
    @Query(value = "SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d 00:00:00') AS bucket, " +
               "MAX(co_value) AS maxVal, MIN(co_value) AS minVal, AVG(co_value) AS avgVal " +
               "FROM sensor_readings " +
               "WHERE device_id = :deviceId AND recorded_at BETWEEN :startTime AND :endTime " +
               "GROUP BY bucket ORDER BY bucket ASC", nativeQuery = true)
    List<Object[]> findDailyCo(@Param("deviceId") Long deviceId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    // Gas - Hourly
    @Query(value = "SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d %H:00:00') AS bucket, " +
               "MAX(gas_value) AS maxVal, MIN(gas_value) AS minVal, AVG(gas_value) AS avgVal " +
               "FROM sensor_readings " +
               "WHERE device_id = :deviceId AND recorded_at BETWEEN :startTime AND :endTime " +
               "GROUP BY bucket ORDER BY bucket ASC", nativeQuery = true)
    List<Object[]> findHourlyGas(@Param("deviceId") Long deviceId,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);

    // Gas - Daily
    @Query(value = "SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d 00:00:00') AS bucket, " +
               "MAX(gas_value) AS maxVal, MIN(gas_value) AS minVal, AVG(gas_value) AS avgVal " +
               "FROM sensor_readings " +
               "WHERE device_id = :deviceId AND recorded_at BETWEEN :startTime AND :endTime " +
               "GROUP BY bucket ORDER BY bucket ASC", nativeQuery = true)
    List<Object[]> findDailyGas(@Param("deviceId") Long deviceId,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);

    // Temperature - Hourly
    @Query(value = "SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d %H:00:00') AS bucket, " +
               "MAX(temperature) AS maxVal, MIN(temperature) AS minVal, AVG(temperature) AS avgVal " +
               "FROM sensor_readings " +
               "WHERE device_id = :deviceId AND recorded_at BETWEEN :startTime AND :endTime " +
               "GROUP BY bucket ORDER BY bucket ASC", nativeQuery = true)
    List<Object[]> findHourlyTemp(@Param("deviceId") Long deviceId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    // Temperature - Daily
    @Query(value = "SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d 00:00:00') AS bucket, " +
               "MAX(temperature) AS maxVal, MIN(temperature) AS minVal, AVG(temperature) AS avgVal " +
               "FROM sensor_readings " +
               "WHERE device_id = :deviceId AND recorded_at BETWEEN :startTime AND :endTime " +
               "GROUP BY bucket ORDER BY bucket ASC", nativeQuery = true)
    List<Object[]> findDailyTemp(@Param("deviceId") Long deviceId,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);

    // Humidity - Hourly
    @Query(value = "SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d %H:00:00') AS bucket, " +
               "MAX(humidity) AS maxVal, MIN(humidity) AS minVal, AVG(humidity) AS avgVal " +
               "FROM sensor_readings " +
               "WHERE device_id = :deviceId AND recorded_at BETWEEN :startTime AND :endTime " +
               "GROUP BY bucket ORDER BY bucket ASC", nativeQuery = true)
    List<Object[]> findHourlyHumidity(@Param("deviceId") Long deviceId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    // Humidity - Daily
    @Query(value = "SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d 00:00:00') AS bucket, " +
               "MAX(humidity) AS maxVal, MIN(humidity) AS minVal, AVG(humidity) AS avgVal " +
               "FROM sensor_readings " +
               "WHERE device_id = :deviceId AND recorded_at BETWEEN :startTime AND :endTime " +
               "GROUP BY bucket ORDER BY bucket ASC", nativeQuery = true)
    List<Object[]> findDailyHumidity(@Param("deviceId") Long deviceId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);
}
