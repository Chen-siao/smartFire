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
 * 传感器数据Controller单元测试
 */
@WebMvcTest(SensorController.class)
class SensorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    // ========== 查询测试 ==========

    @Test
    void testListAll() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/sensors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testListByDeviceId() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/sensors?deviceId=SF001"))
                .andExpect(status().isOk());
    }

    @Test
    void testListByKeyword() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/sensors?keyword=25.5"))
                .andExpect(status().isOk());
    }

    @Test
    void testListWithDefaultLimit() throws Exception {
        // 默认limit=50
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/sensors"))
                .andExpect(status().isOk());
    }

    @Test
    void testListWithCustomLimit() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/sensors?limit=10"))
                .andExpect(status().isOk());
    }

    // ========== 新增测试 ==========

    @Test
    void testAddSensorRecord_Success() throws Exception {
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        String json = "{"
                + "\"deviceId\":\"SF001\","
                + "\"temperature\":25.5,"
                + "\"humidity\":60.0,"
                + "\"co2\":400,"
                + "\"smoke\":0,"
                + "\"light\":2000,"
                + "\"fireType\":0,"
                + "\"alertLevel\":0,"
                + "\"dist\":300,"
                + "\"confidence\":0.95"
                + "}";

        mockMvc.perform(post("/api/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("新增传感器数据成功"));
    }

    // ========== 修改测试 ==========

    @Test
    void testUpdateSensorRecord_Success() throws Exception {
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), anyLong()))
                .thenReturn(1);

        String json = "{"
                + "\"deviceId\":\"SF001\","
                + "\"temperature\":99.9,"
                + "\"humidity\":10.0,"
                + "\"co2\":999,"
                + "\"smoke\":999,"
                + "\"light\":100,"
                + "\"fireType\":1,"
                + "\"alertLevel\":3,"
                + "\"dist\":5,"
                + "\"confidence\":0.99"
                + "}";

        mockMvc.perform(put("/api/sensors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("修改传感器数据成功"));
    }

    // ========== 删除测试 ==========

    @Test
    void testDeleteSensorRecord_Success() throws Exception {
        when(jdbcTemplate.update(anyString(), anyLong()))
                .thenReturn(1);

        mockMvc.perform(delete("/api/sensors/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("删除传感器测试记录成功"));
    }
}
