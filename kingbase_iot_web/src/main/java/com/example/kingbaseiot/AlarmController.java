package com.example.kingbaseiot;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/alarms")
public class AlarmController {
    private final JdbcTemplate jdbcTemplate;

    public AlarmController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/{id}")
    public AlarmRecord getById(@PathVariable Long id) {
        List<AlarmRecord> list = jdbcTemplate.query("""
                SELECT id, device_id, alarm_type, alarm_level, alarm_content,
                       handle_status, handle_remark, alarm_time
                FROM alarm_record
                WHERE id = ?
                """, (rs, rowNum) -> {
            AlarmRecord a = new AlarmRecord();
            a.setId(rs.getLong("id"));
            a.setDeviceId(rs.getString("device_id"));
            a.setAlarmType(rs.getString("alarm_type"));
            int level = rs.getInt("alarm_level");
            if (!rs.wasNull()) a.setAlarmLevel(level);
            a.setAlarmContent(rs.getString("alarm_content"));
            a.setHandleStatus(rs.getString("handle_status"));
            a.setHandleRemark(rs.getString("handle_remark"));
            Timestamp ts = rs.getTimestamp("alarm_time");
            if (ts != null) a.setAlarmTime(ts.toLocalDateTime());
            return a;
        }, id);
        return list.isEmpty() ? null : list.get(0);
    }

    @GetMapping
    public List<AlarmRecord> list(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String alarmType,
            @RequestParam(required = false) String handleStatus,
            @RequestParam(required = false) String keyword) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, device_id, alarm_type, alarm_level, alarm_content,
                       handle_status, handle_remark, alarm_time
                FROM alarm_record WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (deviceId != null && !deviceId.isBlank()) {
            sql.append(" AND device_id ILIKE ?");
            params.add("%" + deviceId.trim() + "%");
        }
        if (alarmType != null && !alarmType.isBlank()) {
            sql.append(" AND alarm_type ILIKE ?");
            params.add("%" + alarmType.trim() + "%");
        }
        if (handleStatus != null && !handleStatus.isBlank()) {
            sql.append(" AND handle_status = ?");
            params.add(handleStatus.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (alarm_content ILIKE ? OR alarm_type ILIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
        }
        sql.append(" ORDER BY id DESC");

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            AlarmRecord a = new AlarmRecord();
            a.setId(rs.getLong("id"));
            a.setDeviceId(rs.getString("device_id"));
            a.setAlarmType(rs.getString("alarm_type"));
            int level = rs.getInt("alarm_level");
            if (!rs.wasNull()) a.setAlarmLevel(level);
            a.setAlarmContent(rs.getString("alarm_content"));
            a.setHandleStatus(rs.getString("handle_status"));
            a.setHandleRemark(rs.getString("handle_remark"));
            Timestamp ts = rs.getTimestamp("alarm_time");
            if (ts != null) a.setAlarmTime(ts.toLocalDateTime());
            return a;
        }, params.toArray());
    }

    @PostMapping
    public String add(@RequestBody AlarmRecord alarm) {
        jdbcTemplate.update("""
                INSERT INTO alarm_record(device_id, alarm_type, alarm_level, alarm_content, handle_status)
                VALUES (?, ?, ?, ?, ?)
                """, alarm.getDeviceId(), alarm.getAlarmType(), alarm.getAlarmLevel(),
                alarm.getAlarmContent(), alarm.getHandleStatus() == null ? "未处理" : alarm.getHandleStatus());
        return "新增报警成功";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @RequestBody AlarmRecord alarm) {
        jdbcTemplate.update("""
                UPDATE alarm_record
                SET handle_status = ?, handle_remark = ?
                WHERE id = ?
                """, alarm.getHandleStatus(), alarm.getHandleRemark(), id);
        return "修改报警处理状态成功";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        jdbcTemplate.update("DELETE FROM alarm_record WHERE id = ?", id);
        return "删除报警成功";
    }
}
