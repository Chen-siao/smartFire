package com.example.kingbaseiot;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 报警记录实体类单元测试
 * 测试Getter/Setter、默认值
 */
class AlarmRecordTest {

    @Test
    void testGettersAndSetters() {
        AlarmRecord a = new AlarmRecord();
        a.setId(1L);
        a.setDeviceId("SF001");
        a.setAlarmType("火情检测");
        a.setAlarmLevel(3);
        a.setAlarmContent("检测到明火");
        a.setHandleStatus("未处理");
        a.setHandleRemark("自动生成");

        assertEquals(1L, a.getId());
        assertEquals("SF001", a.getDeviceId());
        assertEquals("火情检测", a.getAlarmType());
        assertEquals(3, a.getAlarmLevel());
        assertEquals("检测到明火", a.getAlarmContent());
        assertEquals("未处理", a.getHandleStatus());
        assertEquals("自动生成", a.getHandleRemark());
    }

    @Test
    void testDefaultHandleStatus() {
        AlarmRecord a = new AlarmRecord();
        // 实体类不设默认值，由数据库DEFAULT子句设置
        assertNull(a.getHandleStatus());
    }

    @Test
    void testAlarmTimeInitiallyNull() {
        AlarmRecord a = new AlarmRecord();
        // alarmTime由数据库DEFAULT CURRENT_TIMESTAMP设置
        assertNull(a.getAlarmTime());
    }

    @Test
    void testSetAlarmTime() {
        AlarmRecord a = new AlarmRecord();
        LocalDateTime now = LocalDateTime.now();
        a.setAlarmTime(now);
        assertEquals(now, a.getAlarmTime());
    }
}
