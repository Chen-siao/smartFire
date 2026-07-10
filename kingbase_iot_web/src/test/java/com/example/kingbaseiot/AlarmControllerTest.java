package com.example.kingbaseiot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 报警管理Controller单元测试
 */
@WebMvcTest(AlarmController.class)
class AlarmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    // ========== 查询测试 ==========

    @Test
    void testListAll() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/alarms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testListByDeviceId() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/alarms?deviceId=SF001"))
                .andExpect(status().isOk());
    }

    @Test
    void testListByAlarmType() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/alarms?alarmType=温度过高"))
                .andExpect(status().isOk());
    }

    @Test
    void testListByHandleStatus() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/alarms?handleStatus=未处理"))
                .andExpect(status().isOk());
    }

    @Test
    void testListByMultipleFilters() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/alarms?deviceId=SF001&alarmType=火情检测&handleStatus=未处理"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetById_NotFound() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/alarms/99999"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    // ========== 新增测试 ==========

    @Test
    void testAddAlarm_Success() throws Exception {
        when(jdbcTemplate.update(anyString(), any(), any(), anyInt(), any(), any()))
                .thenReturn(1);

        String json = "{"
                + "\"deviceId\":\"SF001\","
                + "\"alarmType\":\"温度过高\","
                + "\"alarmLevel\":2,"
                + "\"alarmContent\":\"温度异常偏高：55.0°C\","
                + "\"handleStatus\":\"未处理\""
                + "}";

        mockMvc.perform(post("/api/alarms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("新增报警成功"));
    }

    @Test
    void testAddAlarm_DefaultStatus() throws Exception {
        when(jdbcTemplate.update(anyString(), any(), any(), anyInt(), any(), any()))
                .thenReturn(1);

        // 不传handleStatus，Controller默认设置"未处理"
        String json = "{"
                + "\"deviceId\":\"SF002\","
                + "\"alarmType\":\"烟雾异常\","
                + "\"alarmLevel\":2,"
                + "\"alarmContent\":\"检测到烟雾浓度异常\""
                + "}";

        mockMvc.perform(post("/api/alarms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    // ========== 修改测试 ==========

    @Test
    void testUpdateAlarmStatus_Success() throws Exception {
        when(jdbcTemplate.update(anyString(), any(), any(), anyLong()))
                .thenReturn(1);

        String json = "{"
                + "\"handleStatus\":\"已处理\","
                + "\"handleRemark\":\"现场确认无火情\""
                + "}";

        mockMvc.perform(put("/api/alarms/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("修改报警处理状态成功"));
    }

    // ========== 删除测试 ==========

    @Test
    void testDeleteAlarm_Success() throws Exception {
        when(jdbcTemplate.update(anyString(), anyLong()))
                .thenReturn(1);

        mockMvc.perform(delete("/api/alarms/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("删除报警成功"));
    }
}
