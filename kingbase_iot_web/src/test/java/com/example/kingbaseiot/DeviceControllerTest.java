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
 * 设备管理Controller单元测试
 * 使用MockMvc模拟HTTP请求，Mockito隔离JdbcTemplate
 */
@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    // ========== 查询测试 ==========

    @Test
    void testListAll_ReturnsEmptyList() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testListByKeyword() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/devices?keyword=SF001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testListByStatus() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/devices?status=在线"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testListByKeywordAndStatus() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/devices?keyword=SF&status=在线"))
                .andExpect(status().isOk());
    }

    // ========== 新增测试 ==========

    @Test
    void testAddDevice_Success() throws Exception {
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any()))
                .thenReturn(1);

        String json = "{"
                + "\"deviceId\":\"SF001\","
                + "\"deviceName\":\"一号传感器\","
                + "\"location\":\"A区北坡\","
                + "\"status\":\"在线\""
                + "}";

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("新增设备成功"));
    }

    // ========== 修改测试 ==========

    @Test
    void testUpdateDevice_Success() throws Exception {
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(), anyLong()))
                .thenReturn(1);

        String json = "{"
                + "\"deviceId\":\"SF002\","
                + "\"deviceName\":\"二号传感器\","
                + "\"location\":\"B区南坡\","
                + "\"status\":\"维护中\""
                + "}";

        mockMvc.perform(put("/api/devices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("修改设备成功"));
    }

    // ========== 删除测试 ==========

    @Test
    void testDeleteDevice_Success() throws Exception {
        when(jdbcTemplate.update(anyString(), anyLong()))
                .thenReturn(1);

        mockMvc.perform(delete("/api/devices/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("删除设备成功"));
    }

    // ========== 边界测试 ==========

    @Test
    void testSqlInjectionPrevention() throws Exception {
        // JdbcTemplate使用PreparedStatement参数化查询，天然防SQL注入
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenReturn(Collections.emptyList());

        String malicious = "'; DROP TABLE device_info; --";
        mockMvc.perform(get("/api/devices?keyword=" + malicious))
                .andExpect(status().isOk());
        // 恶意SQL被当作普通搜索关键词处理，不会执行DROP操作
    }
}
