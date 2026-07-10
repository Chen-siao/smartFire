package com.example.kingbaseiot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AlarmCheckService Unit Tests
 * Tests: fire detection, alert level, smoke anomaly, temperature anomaly, dedup
 */
class AlarmCheckServiceTest {

    private JdbcTemplate jdbcTemplate;
    private AlarmCheckService alarmCheckService;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        alarmCheckService = new AlarmCheckService(jdbcTemplate);
    }

    // ==================== Empty data ====================

    @Test
    void testNoSensorData_NoAlarmGenerated() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Timestamp.class)))
                .thenReturn(Collections.emptyList());
        alarmCheckService.checkForAnomalies();
        verify(jdbcTemplate, never()).update(contains("INSERT INTO alarm_record"),
                any(), any(), any(), any(), any());
    }

    // ==================== Fire type detection ====================

    @Test
    void testFireTypeDetected_CreatesAlarm() {
        SensorRecord r = makeRecord(1, 0, BigDecimal.ZERO, new BigDecimal("30.0"));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Timestamp.class)))
                .thenReturn(List.of(r));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any(), any()))
                .thenReturn(0);

        alarmCheckService.checkForAnomalies();

        verify(jdbcTemplate, times(1)).update(
                contains("INSERT INTO alarm_record"),
                anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    void testNoFireType_NoAlarmGeneratedForFire() {
        SensorRecord r = makeRecord(0, 0, BigDecimal.ZERO, new BigDecimal("25.0"));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Timestamp.class)))
                .thenReturn(List.of(r));

        alarmCheckService.checkForAnomalies();

        verify(jdbcTemplate, never()).update(contains("INSERT INTO alarm_record"),
                any(), any(), any(), any(), any());
    }

    // ==================== Alert level detection ====================

    @Test
    void testHighAlertLevel_CreatesAlarm() {
        SensorRecord r = makeRecord(0, 2, BigDecimal.ZERO, new BigDecimal("30.0"));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Timestamp.class)))
                .thenReturn(List.of(r));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any(), any()))
                .thenReturn(0);

        alarmCheckService.checkForAnomalies();

        verify(jdbcTemplate, atLeastOnce()).update(
                contains("INSERT INTO alarm_record"),
                anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    void testLowAlertLevel_NoAlarmForAlert() {
        SensorRecord r = makeRecord(0, 1, BigDecimal.ZERO, new BigDecimal("25.0"));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Timestamp.class)))
                .thenReturn(List.of(r));

        alarmCheckService.checkForAnomalies();

        verify(jdbcTemplate, never()).update(contains("INSERT INTO alarm_record"),
                any(), any(), any(), any(), any());
    }

    // ==================== Smoke anomaly ====================

    @Test
    void testSmokeAboveThreshold_CreatesAlarm() {
        SensorRecord r = makeRecord(0, 0, new BigDecimal("300"), new BigDecimal("35.0"));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Timestamp.class)))
                .thenReturn(List.of(r));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any(), any()))
                .thenReturn(0);

        alarmCheckService.checkForAnomalies();

        verify(jdbcTemplate, times(1)).update(
                contains("INSERT INTO alarm_record"),
                anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    void testSmokeZero_NoAlarm() {
        SensorRecord r = makeRecord(0, 0, BigDecimal.ZERO, new BigDecimal("25.0"));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Timestamp.class)))
                .thenReturn(List.of(r));

        alarmCheckService.checkForAnomalies();

        verify(jdbcTemplate, never()).update(contains("INSERT INTO alarm_record"),
                any(), any(), any(), any(), any());
    }

    // ==================== Temperature anomaly ====================

    @Test
    void testTemperatureAbove50_CreatesAlarm() {
        SensorRecord r = makeRecord(0, 0, BigDecimal.ZERO, new BigDecimal("55.0"));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Timestamp.class)))
                .thenReturn(List.of(r));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any(), any()))
                .thenReturn(0);

        alarmCheckService.checkForAnomalies();

        verify(jdbcTemplate, times(1)).update(
                contains("INSERT INTO alarm_record"),
                anyString(), anyString(), anyInt(), anyString());
    }

    @Test
    void testTemperatureBelow50_NoAlarm() {
        SensorRecord r = makeRecord(0, 0, BigDecimal.ZERO, new BigDecimal("49.0"));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Timestamp.class)))
                .thenReturn(List.of(r));

        alarmCheckService.checkForAnomalies();

        verify(jdbcTemplate, never()).update(contains("INSERT INTO alarm_record"),
                any(), any(), any(), any(), any());
    }

    // ==================== Duplicate alarm prevention ====================

    @Test
    void testDuplicateAlarm_Within60Seconds_Skipped() {
        SensorRecord r = makeRecord(0, 0, BigDecimal.ZERO, new BigDecimal("55.0"));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Timestamp.class)))
                .thenReturn(List.of(r));
        // 60 seconds already has same type alarm
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any(), any()))
                .thenReturn(1);

        alarmCheckService.checkForAnomalies();

        verify(jdbcTemplate, never()).update(contains("INSERT INTO alarm_record"),
                any(), any(), any(), any(), any());
    }

    // ==================== Smoke with level 3 (above 200) ====================

    @Test
    void testSmokeAbove200_Level3Alarm() {
        SensorRecord r = makeRecord(0, 0, new BigDecimal("250"), new BigDecimal("35.0"));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Timestamp.class)))
                .thenReturn(List.of(r));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any(), any()))
                .thenReturn(0);

        alarmCheckService.checkForAnomalies();

        verify(jdbcTemplate, times(1)).update(
                contains("INSERT INTO alarm_record"),
                anyString(), anyString(), eq(3), anyString());
    }

    // ==================== Temperature above 70 = level 3 ====================

    @Test
    void testTemperatureAbove70_Level3Alarm() {
        SensorRecord r = makeRecord(0, 0, BigDecimal.ZERO, new BigDecimal("75.0"));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Timestamp.class)))
                .thenReturn(List.of(r));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any(), any()))
                .thenReturn(0);

        alarmCheckService.checkForAnomalies();

        verify(jdbcTemplate, times(1)).update(
                contains("INSERT INTO alarm_record"),
                anyString(), anyString(), eq(3), anyString());
    }

    // ==================== Multiple triggers ====================

    @Test
    void testMultipleTriggers_MultipleAlarms() {
        // fireType=1 + temperature=55°C → at least 2 alarms
        SensorRecord r = makeRecord(1, 0, BigDecimal.ZERO, new BigDecimal("55.0"));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Timestamp.class)))
                .thenReturn(List.of(r));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any(), any()))
                .thenReturn(0);

        alarmCheckService.checkForAnomalies();

        verify(jdbcTemplate, atLeast(2)).update(
                contains("INSERT INTO alarm_record"),
                anyString(), anyString(), anyInt(), anyString());
    }

    // ==================== Helper ====================

    private SensorRecord makeRecord(int fireType, int alertLevel,
                                     BigDecimal smoke, BigDecimal temperature) {
        SensorRecord r = new SensorRecord();
        r.setId(1L);
        r.setDeviceId("TEST01");
        r.setFireType(fireType);
        r.setAlertLevel(alertLevel);
        r.setSmoke(smoke);
        r.setTemperature(temperature);
        r.setHumidity(new BigDecimal("50.0"));
        r.setCo2(new BigDecimal("400"));
        r.setLight(new BigDecimal("2000"));
        r.setDist(new BigDecimal("300"));
        r.setConfidence(new BigDecimal("0.9"));
        r.setReportTime(LocalDateTime.now());
        return r;
    }
}
