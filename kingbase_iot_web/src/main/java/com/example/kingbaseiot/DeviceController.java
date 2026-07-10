package com.example.kingbaseiot;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final JdbcTemplate jdbcTemplate;

    public DeviceController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<DeviceInfo> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        StringBuilder sql = new StringBuilder("""
                SELECT id, device_id, device_name, location, status, create_time
                FROM device_info WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (device_id ILIKE ? OR device_name ILIKE ? OR location ILIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
            params.add(status.trim());
        }
        sql.append(" ORDER BY id DESC");

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            DeviceInfo d = new DeviceInfo();
            d.setId(rs.getLong("id"));
            d.setDeviceId(rs.getString("device_id"));
            d.setDeviceName(rs.getString("device_name"));
            d.setLocation(rs.getString("location"));
            d.setStatus(rs.getString("status"));
            Timestamp ts = rs.getTimestamp("create_time");
            if (ts != null) d.setCreateTime(ts.toLocalDateTime());
            return d;
        }, params.toArray());
    }

    @PostMapping
    public String add(@RequestBody DeviceInfo device) {
        jdbcTemplate.update("""
                INSERT INTO device_info(device_id, device_name, location, status)
                VALUES (?, ?, ?, ?)
                """, device.getDeviceId(), device.getDeviceName(), device.getLocation(), device.getStatus());
        return "新增设备成功";
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @RequestBody DeviceInfo device) {
        jdbcTemplate.update("""
                UPDATE device_info
                SET device_id = ?, device_name = ?, location = ?, status = ?
                WHERE id = ?
                """, device.getDeviceId(), device.getDeviceName(), device.getLocation(), device.getStatus(), id);
        return "修改设备成功";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        jdbcTemplate.update("DELETE FROM device_info WHERE id = ?", id);
        return "删除设备成功";
    }
}
