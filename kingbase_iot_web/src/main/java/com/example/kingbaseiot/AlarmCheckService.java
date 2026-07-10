package com.example.kingbaseiot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlarmCheckService {

    private static final Logger log = LoggerFactory.getLogger(AlarmCheckService.class);

    private final JdbcTemplate jdbcTemplate;

    public AlarmCheckService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(fixedDelay = 10_000)
    public void checkForAnomalies() {
        // 查询最近 15 秒内的传感器数据
        List<SensorRecord> recent = jdbcTemplate.query("""
                SELECT id, device_id, temperature, humidity, co2, smoke, light,
                       fire_type, alert_level, dist, confidence, alert_history, report_time
                FROM iot_sensor_record
                WHERE report_time >= ?
                ORDER BY id DESC
                LIMIT 30
                """, (rs, rowNum) -> {
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
                }, Timestamp.valueOf(LocalDateTime.now().minusSeconds(15)));

        log.info("定时检测: 最近15秒共{}条传感器数据", recent.size());
        if (recent.isEmpty()) return;

        for (SensorRecord r : recent) {
            checkFireType(r);
            checkAlertLevel(r);
            checkSmoke(r);
            checkTemperature(r);
        }
    }

    // 火情类型 > 0 说明检测到火情
    private void checkFireType(SensorRecord r) {
        if (r.getFireType() == null || r.getFireType() <= 0) return;

        String[] fireTypes = {"", "明火", "阴燃", "电气火灾"};
        String typeName = r.getFireType() < fireTypes.length ? fireTypes[r.getFireType()] : "未知火情(" + r.getFireType() + ")";
        String content = String.format("传感器检测到%s，距离约%d米，置信度%s",
                typeName,
                r.getDist() != null ? r.getDist().intValue() : 0,
                r.getConfidence() != null ? r.getConfidence().toString() : "未知");

        createAlarmIfNew(r.getDeviceId(), "火情检测", 3, content);
    }

    // 传感器自身告警等级 >= 2
    private void checkAlertLevel(SensorRecord r) {
        if (r.getAlertLevel() == null || r.getAlertLevel() < 2) return;

        String levelName = r.getAlertLevel() == 2 ? "中" : "高";
        String content = String.format("传感器上报%s等级告警，当前温度%s°C、湿度%s%%、烟雾%s",
                levelName,
                r.getTemperature() != null ? r.getTemperature().toString() : "-",
                r.getHumidity() != null ? r.getHumidity().toString() : "-",
                r.getSmoke() != null ? r.getSmoke().toString() : "-");

        createAlarmIfNew(r.getDeviceId(), "传感器告警", r.getAlertLevel(), content);
    }

    // 烟雾 > 0
    private void checkSmoke(SensorRecord r) {
        if (r.getSmoke() == null || r.getSmoke().compareTo(BigDecimal.ZERO) <= 0) return;

        String content = String.format("检测到烟雾浓度异常：%s，当前温度%s°C",
                r.getSmoke().toString(),
                r.getTemperature() != null ? r.getTemperature().toString() : "-");

        int level = r.getSmoke().compareTo(new BigDecimal("200")) > 0 ? 3 : 2;
        createAlarmIfNew(r.getDeviceId(), "烟雾异常", level, content);
    }

    // 温度 > 50°C
    private void checkTemperature(SensorRecord r) {
        if (r.getTemperature() == null || r.getTemperature().compareTo(new BigDecimal("50")) <= 0) return;

        String content = String.format("温度异常偏高：%s°C，湿度%s%%",
                r.getTemperature().toString(),
                r.getHumidity() != null ? r.getHumidity().toString() : "-");

        int level = r.getTemperature().compareTo(new BigDecimal("70")) > 0 ? 3 : 2;
        createAlarmIfNew(r.getDeviceId(), "温度过高", level, content);
    }

    // 防止重复报警：同一设备同一类型在 60 秒内不重复创建
    private void createAlarmIfNew(String deviceId, String alarmType, int alarmLevel, String alarmContent) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM alarm_record
                WHERE device_id = ? AND alarm_type = ? AND alarm_time >= ?
                """, Integer.class,
                deviceId, alarmType, Timestamp.valueOf(LocalDateTime.now().minusSeconds(60)));

        if (count != null && count > 0) return;

        jdbcTemplate.update("""
                INSERT INTO alarm_record(device_id, alarm_type, alarm_level, alarm_content, handle_status)
                VALUES (?, ?, ?, ?, '未处理')
                """, deviceId, alarmType, alarmLevel, alarmContent);

        log.info("自动生成报警: device={}, type={}, level={}, content={}",
                deviceId, alarmType, alarmLevel, alarmContent);
    }
}
