package com.example.kingbaseiot;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {
    private final JdbcTemplate jdbcTemplate;

    public SensorController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<SensorRecord> list(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "50") int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, device_id, temperature, humidity, co2, smoke, light,
                       fire_type, alert_level, dist, confidence, alert_history, report_time
                FROM iot_sensor_record WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (deviceId != null && !deviceId.isBlank()) {
            sql.append(" AND device_id ILIKE ?");
            params.add("%" + deviceId.trim() + "%");
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (CAST(temperature AS TEXT) ILIKE ? OR CAST(humidity AS TEXT) ILIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }
        sql.append(" ORDER BY id DESC LIMIT ?");
        params.add(limit);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            SensorRecord r = new SensorRecord();
            r.setId(rs.getLong("id"));
            r.setDeviceId(rs.getString("device_id"));
            r.setTemperature(rs.getBigDecimal("temperature"));
            r.setHumidity(rs.getBigDecimal("humidity"));
            r.setCo2(rs.getBigDecimal("co2"));
            r.setSmoke(rs.getBigDecimal("smoke"));
            r.setLight(rs.getBigDecimal("light"));
            int fireType = rs.getInt("fire_type");
            if (!rs.wasNull()) r.setFireType(fireType);
            int alertLevel = rs.getInt("alert_level");
            if (!rs.wasNull()) r.setAlertLevel(alertLevel);
            r.setDist(rs.getBigDecimal("dist"));
            r.setConfidence(rs.getBigDecimal("confidence"));
            r.setAlertHistory(rs.getString("alert_history"));
            Timestamp ts = rs.getTimestamp("report_time");
            if (ts != null) r.setReportTime(ts.toLocalDateTime());
            return r;
        }, params.toArray());
    }

    @PostMapping
    public String add(@RequestBody SensorRecord record) {
        jdbcTemplate.update("""
                INSERT INTO iot_sensor_record(device_id, temperature, humidity, co2, smoke, light,
                       fire_type, alert_level, dist, confidence, alert_history, report_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """,
                record.getDeviceId(), record.getTemperature(), record.getHumidity(),
                record.getCo2(), record.getSmoke(), record.getLight(),
                record.getFireType(), record.getAlertLevel(), record.getDist(),
                record.getConfidence(), record.getAlertHistory());
        return "新增传感器数据成功";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @RequestBody SensorRecord record) {
        jdbcTemplate.update("""
                UPDATE iot_sensor_record
                SET device_id = ?, temperature = ?, humidity = ?, co2 = ?, smoke = ?,
                    light = ?, fire_type = ?, alert_level = ?, dist = ?, confidence = ?
                WHERE id = ?
                """,
                record.getDeviceId(), record.getTemperature(), record.getHumidity(),
                record.getCo2(), record.getSmoke(), record.getLight(),
                record.getFireType(), record.getAlertLevel(), record.getDist(),
                record.getConfidence(), id);
        return "修改传感器数据成功";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        jdbcTemplate.update("DELETE FROM iot_sensor_record WHERE id = ?", id);
        return "删除传感器测试记录成功";
    }
}
