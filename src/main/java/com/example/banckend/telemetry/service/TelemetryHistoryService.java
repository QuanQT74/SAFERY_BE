package com.example.banckend.telemetry.service;

import com.example.banckend.conmon.enums.DeviceErrorCode;
import com.example.banckend.conmon.exception.ResourceNotFoundException;
import com.example.banckend.device.entity.Device;
import com.example.banckend.device.repository.DeviceRepository;
import com.example.banckend.telemetry.dto.response.AggregatedDataResponse;
import com.example.banckend.telemetry.dto.response.AggregationResult;
import com.example.banckend.telemetry.dto.response.SensorAggregatedResponse;
import com.example.banckend.telemetry.dto.response.SensorStatsResponse;
import com.example.banckend.telemetry.dto.response.SensorValueResponse;
import com.example.banckend.telemetry.dto.response.TelemetryHistoryResponse;
import com.example.banckend.telemetry.dto.response.TelemetryStatsResponse;
import com.example.banckend.telemetry.entity.SensorReading;
import com.example.banckend.telemetry.mapper.TelemetryMapper;
import com.example.banckend.telemetry.repository.SensorReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TelemetryHistoryService {

    private final SensorReadingRepository sensorReadingRepository;
    private final DeviceRepository deviceRepository;
    private final TelemetryMapper telemetryMapper;

    // Ngưỡng cảnh báo OSHA
    private static final double GAS_WARNING_THRESHOLD = 500;
    private static final double CO_WARNING_THRESHOLD = 20;
    private static final double TEMP_WARNING_THRESHOLD = 40;

    @Transactional(readOnly = true)
    public List<TelemetryHistoryResponse> getTelemetryHistory(
            String deviceCode,
            Long userId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int limit) {

        Device device = getDeviceOrThrow(deviceCode, userId);

        if (limit > 1000) {
            limit = 1000;
        }

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "recordedAt"));

        Page<SensorReading> readings;
        if (startTime != null && endTime != null) {
            readings = sensorReadingRepository.findByDeviceIdAndTimeRange(device.getId(), startTime, endTime, pageable);
        } else {
            List<SensorReading> latestReadings = sensorReadingRepository.findLatestByDeviceId(device.getId(), pageable);
            return latestReadings.stream()
                    .map(telemetryMapper::toHistoryResponse)
                    .collect(Collectors.toList());
        }

        return readings.stream()
                .map(telemetryMapper::toHistoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TelemetryStatsResponse getTelemetryStats(String deviceCode, Long userId, String period) {
        Device device = getDeviceOrThrow(deviceCode, userId);

        // Xác định khoảng thời gian
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        switch (period.toLowerCase()) {
            case "7d":
            case "7day":
            case "7days":
                startTime = now.minusDays(7);
                break;
            case "30d":
            case "30day":
            case "30days":
                startTime = now.minusDays(30);
                break;
            case "24h":
            case "1d":
            case "1day":
            default:
                startTime = now.minusHours(24);
                period = "24h";
        }

        // Lấy bản ghi mới nhất (hiện tại)
        Pageable latestPageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "recordedAt"));
        List<SensorReading> latestReadings = sensorReadingRepository.findLatestByDeviceId(device.getId(), latestPageable);

        SensorReading currentReading = latestReadings.isEmpty() ? null : latestReadings.get(0);

        // Lấy thống kê
        List<Object[]> statsList = sensorReadingRepository.findStatsByDeviceIdAndTimeRange(device.getId(), startTime, now);
        AggregationResult aggregation;
        if (statsList != null && !statsList.isEmpty()) {
            Object[] stats = statsList.get(0);
            if (stats != null && stats.length >= 12 && stats[0] != null) {
                aggregation = new AggregationResult(
                        stats[0], stats[1], stats[2],  // gas: max, min, avg
                        stats[3], stats[4], stats[5],  // co: max, min, avg
                        stats[6], stats[7], stats[8],  // temp: max, min, avg
                        stats[9], stats[10], stats[11] // humidity: max, min, avg
                );
            } else {
                aggregation = new AggregationResult(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            }
        } else {
            aggregation = new AggregationResult(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        // Nếu các giá trị thống kê đều bằng 0 (có thể do query không trả về), tính thủ công từ các bản ghi gần nhất
        if (aggregation.getMaxGas() == 0 && aggregation.getMinGas() == 0 && aggregation.getAvgGas() == 0 &&
                aggregation.getMaxCo() == 0 && aggregation.getMinCo() == 0 && aggregation.getAvgCo() == 0 &&
                aggregation.getMaxTemp() == 0 && aggregation.getMinTemp() == 0 && aggregation.getAvgTemp() == 0 &&
                aggregation.getMaxHumidity() == 0 && aggregation.getMinHumidity() == 0 && aggregation.getAvgHumidity() == 0) {
            // Lấy một số bản ghi gần nhất (max 100) để tính thống kê
            Pageable recentPage = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "recordedAt"));
            List<SensorReading> recentReadings = sensorReadingRepository.findLatestByDeviceId(device.getId(), recentPage);
            if (!recentReadings.isEmpty()) {
                // Tính max/min/avg cho mỗi trường
                double maxGas = recentReadings.stream().mapToDouble(r -> r.getGasValue() != null ? r.getGasValue() : 0).max().orElse(0);
                double minGas = recentReadings.stream().mapToDouble(r -> r.getGasValue() != null ? r.getGasValue() : 0).min().orElse(0);
                double avgGas = recentReadings.stream().mapToDouble(r -> r.getGasValue() != null ? r.getGasValue() : 0).average().orElse(0);
                double maxCo = recentReadings.stream().mapToDouble(r -> r.getCoValue() != null ? r.getCoValue() : 0).max().orElse(0);
                double minCo = recentReadings.stream().mapToDouble(r -> r.getCoValue() != null ? r.getCoValue() : 0).min().orElse(0);
                double avgCo = recentReadings.stream().mapToDouble(r -> r.getCoValue() != null ? r.getCoValue() : 0).average().orElse(0);
                double maxTemp = recentReadings.stream().mapToDouble(r -> r.getTemperature() != null ? r.getTemperature() : 0).max().orElse(0);
                double minTemp = recentReadings.stream().mapToDouble(r -> r.getTemperature() != null ? r.getTemperature() : 0).min().orElse(0);
                double avgTemp = recentReadings.stream().mapToDouble(r -> r.getTemperature() != null ? r.getTemperature() : 0).average().orElse(0);
                double maxHum = recentReadings.stream().mapToDouble(r -> r.getHumidity() != null ? r.getHumidity() : 0).max().orElse(0);
                double minHum = recentReadings.stream().mapToDouble(r -> r.getHumidity() != null ? r.getHumidity() : 0).min().orElse(0);
                double avgHum = recentReadings.stream().mapToDouble(r -> r.getHumidity() != null ? r.getHumidity() : 0).average().orElse(0);
                aggregation = new AggregationResult(maxGas, minGas, avgGas, maxCo, minCo, avgCo, maxTemp, minTemp, avgTemp, maxHum, minHum, avgHum);
            }
        }

        // Build response
        return TelemetryStatsResponse.builder()
                // Gas
                .currentGas(currentReading != null ? currentReading.getGasValue() : 0.0)
                .maxGas(aggregation.getMaxGas())
                .minGas(aggregation.getMinGas())
                .avgGas(aggregation.getAvgGas())
                .gasWarning(currentReading != null && currentReading.getGasValue() > GAS_WARNING_THRESHOLD)
                // CO
                .currentCo(currentReading != null ? currentReading.getCoValue() : 0.0)
                .maxCo(aggregation.getMaxCo())
                .minCo(aggregation.getMinCo())
                .avgCo(aggregation.getAvgCo())
                .coWarning(currentReading != null && currentReading.getCoValue() > CO_WARNING_THRESHOLD)
                // Temperature
                .currentTemp(currentReading != null ? currentReading.getTemperature() : 0.0)
                .maxTemp(aggregation.getMaxTemp())
                .minTemp(aggregation.getMinTemp())
                .avgTemp(aggregation.getAvgTemp())
                .tempWarning(currentReading != null && currentReading.getTemperature() > TEMP_WARNING_THRESHOLD)
                // Humidity
                .currentHumidity(currentReading != null ? currentReading.getHumidity() : 0.0)
                .maxHumidity(aggregation.getMaxHumidity())
                .minHumidity(aggregation.getMinHumidity())
                .avgHumidity(aggregation.getAvgHumidity())
                // Flame & AI
                .flameDetected(currentReading != null && currentReading.getFlameDetected())
                .aiDetectedFire(currentReading != null && currentReading.getAiDetectedFire())
                // Period
                .periodStart(startTime)
                .periodEnd(now)
                .build();
    }

    private Device getDeviceOrThrow(String deviceCode, Long userId) {
        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new ResourceNotFoundException(DeviceErrorCode.DEVICE_NOT_FOUND));

        if (device.getOwnerUserId() == null || !device.getOwnerUserId().equals(userId)) {
            throw new ResourceNotFoundException(DeviceErrorCode.DEVICE_NOT_FOUND);
        }

        return device;
    }

    @Transactional(readOnly = true)
    public List<AggregatedDataResponse> getAggregatedTelemetry(String deviceCode, Long userId, String period) {
        Device device = getDeviceOrThrow(deviceCode, userId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        boolean hourly = true; // default 24h hourly

        switch (period.toLowerCase()) {
            case "7d":
            case "7day":
            case "7days":
                startTime = now.minusDays(7);
                hourly = false; // daily aggregation
                break;
            case "30d":
            case "30day":
            case "30days":
                startTime = now.minusDays(30);
                hourly = false;
                break;
            case "24h":
            case "1d":
            case "1day":
            default:
                startTime = now.minusHours(24);
                hourly = true;
                break;
        }

        List<Object[]> rows;
        if (hourly) {
            rows = sensorReadingRepository.findHourlyAggregatedData(device.getId(), startTime, now);
        } else {
            rows = sensorReadingRepository.findDailyAggregatedData(device.getId(), startTime, now);
        }

        return rows.stream()
                .map(AggregatedDataResponse::fromObjectArray)
                .collect(Collectors.toList());
    }

    // ----- Per‑sensor aggregated data -----
    @Transactional(readOnly = true)
    public List<SensorAggregatedResponse> getAggregatedSensor(String deviceCode, Long userId, String sensor, String period) {
        Device device = getDeviceOrThrow(deviceCode, userId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        boolean hourly = true;
        switch (period.toLowerCase()) {
            case "7d": case "7day": case "7days":
                start = now.minusDays(7); hourly = false; break;
            case "30d": case "30day": case "30days":
                start = now.minusDays(30); hourly = false; break;
            default:
                start = now.minusHours(24); hourly = true; break;
        }
        List<Object[]> rows;
        switch (sensor.toLowerCase()) {
            case "co": rows = hourly ? sensorReadingRepository.findHourlyCo(device.getId(), start, now) : sensorReadingRepository.findDailyCo(device.getId(), start, now); break;
            case "gas": rows = hourly ? sensorReadingRepository.findHourlyGas(device.getId(), start, now) : sensorReadingRepository.findDailyGas(device.getId(), start, now); break;
            case "temp": rows = hourly ? sensorReadingRepository.findHourlyTemp(device.getId(), start, now) : sensorReadingRepository.findDailyTemp(device.getId(), start, now); break;
            case "humidity": rows = hourly ? sensorReadingRepository.findHourlyHumidity(device.getId(), start, now) : sensorReadingRepository.findDailyHumidity(device.getId(), start, now); break;
            default: throw new IllegalArgumentException("Unsupported sensor: " + sensor);
        }
        return rows.stream().map(SensorAggregatedResponse::fromObjectArray).collect(Collectors.toList());
    }

    // ----- Per‑sensor raw summary (stats) -----
    @Transactional(readOnly = true)
    public SensorStatsResponse getRawSensorStats(String deviceCode, Long userId, String sensor, String period) {
        Device device = getDeviceOrThrow(deviceCode, userId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        switch (period.toLowerCase()) {
            case "7d": case "7day": case "7days": start = now.minusDays(7); break;
            case "30d": case "30day": case "30days": start = now.minusDays(30); break;
            default: start = now.minusHours(24); break;
        }

        // 1. Current value (most recent reading)
        Pageable latestPage = org.springframework.data.domain.PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "recordedAt"));
        List<SensorReading> latest = sensorReadingRepository.findLatestByDeviceId(device.getId(), latestPage);
        double current = 0.0;
        if (!latest.isEmpty()) {
            current = extractSensorValue(latest.get(0), sensor);
        }

        // 2. Stats (max/min/avg)
        List<Object[]> statsList = sensorReadingRepository.findStatsByDeviceIdAndTimeRange(device.getId(), start, now);
        double max = 0.0, min = 0.0, avg = 0.0;
        if (statsList != null && !statsList.isEmpty()) {
            Object[] stats = statsList.get(0);
            if (stats != null && stats.length >= 12 && stats[0] != null) {
                switch (sensor.toLowerCase()) {
                    case "co":
                        max = toDouble(stats[3]); min = toDouble(stats[4]); avg = toDouble(stats[5]); break;
                    case "gas":
                        max = toDouble(stats[0]); min = toDouble(stats[1]); avg = toDouble(stats[2]); break;
                    case "temp":
                        max = toDouble(stats[6]); min = toDouble(stats[7]); avg = toDouble(stats[8]); break;
                    case "humidity":
                        max = toDouble(stats[9]); min = toDouble(stats[10]); avg = toDouble(stats[11]); break;
                }
            }
        }

        return SensorStatsResponse.builder()
                .sensor(sensor)
                .period(period)
                .currentValue(current)
                .maxValue(max)
                .minValue(min)
                .avgValue(avg)
                .build();
    }

    private double extractSensorValue(SensorReading r, String sensor) {
        switch (sensor.toLowerCase()) {
            case "co": return r.getCoValue() != null ? r.getCoValue() : 0.0;
            case "gas": return r.getGasValue() != null ? r.getGasValue() : 0.0;
            case "temp": return r.getTemperature() != null ? r.getTemperature() : 0.0;
            case "humidity": return r.getHumidity() != null ? r.getHumidity() : 0.0;
            default: return 0.0;
        }
    }

    private double toDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        try { return Double.parseDouble(obj.toString()); } catch (Exception e) { return 0.0; }
    }

    // ----- Per‑sensor raw readings -----
    @Transactional(readOnly = true)
    public List<SensorValueResponse> getRawSensorReadings(String deviceCode, Long userId, String sensor, String period) {
        Device device = getDeviceOrThrow(deviceCode, userId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;

        // Giới hạn số bản ghi tùy theo period
        // 24h: lấy raw (5s/lần ≈ 720 bản ghi) → limit 1000
        // 7d/30d: dữ liệu quá nhiều, lấy theo từng giờ (aggregated khuyến khích dùng hơn)
        //         nhưng nếu user vẫn gọi raw, chúng ta lấy tối đa 2000 bản ghi trải đều trong khoảng
        int limit;
        switch (period.toLowerCase()) {
            case "7d": case "7day": case "7days":
                start = now.minusDays(7);
                limit = 2000;
                break;
            case "30d": case "30day": case "30days":
                start = now.minusDays(30);
                limit = 2000;
                break;
            default: // 24h
                start = now.minusHours(24);
                limit = 1000;
                break;
        }

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "recordedAt"));
        Page<SensorReading> page = sensorReadingRepository.findByDeviceIdAndTimeRange(device.getId(), start, now, pageable);

        return page.stream().map(r -> {
            double val = 0.0;
            String s = sensor.toLowerCase();
            if (s.equals("co")) val = r.getCoValue() != null ? r.getCoValue() : 0.0;
            else if (s.equals("gas")) val = r.getGasValue() != null ? r.getGasValue() : 0.0;
            else if (s.equals("temp")) val = r.getTemperature() != null ? r.getTemperature() : 0.0;
            else if (s.equals("humidity")) val = r.getHumidity() != null ? r.getHumidity() : 0.0;

            return SensorValueResponse.builder()
                    .recordedAt(r.getRecordedAt())
                    .value(val)
                    .build();
        }).collect(Collectors.toList());
    }
}
