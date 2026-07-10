package com.example.kingbaseiot;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 设备信息实体类单元测试
 * 测试Getter/Setter方法、字段完整性
 */
class DeviceInfoTest {

    @Test
    void testGettersAndSetters() {
        DeviceInfo d = new DeviceInfo();
        d.setId(1L);
        d.setDeviceId("SF001");
        d.setDeviceName("一号传感器");
        d.setLocation("A区北坡");
        d.setStatus("在线");

        assertEquals(1L, d.getId());
        assertEquals("SF001", d.getDeviceId());
        assertEquals("一号传感器", d.getDeviceName());
        assertEquals("A区北坡", d.getLocation());
        assertEquals("在线", d.getStatus());
    }

    @Test
    void testAllFieldsNotNull_AfterSet() {
        DeviceInfo d = new DeviceInfo();
        d.setId(100L);
        d.setDeviceId("DEV-TEST-001");
        d.setDeviceName("测试设备");
        d.setLocation("测试位置");
        d.setStatus("维护中");

        assertNotNull(d.getId());
        assertNotNull(d.getDeviceId());
        assertNotNull(d.getDeviceName());
        assertNotNull(d.getLocation());
        assertNotNull(d.getStatus());
    }

    @Test
    void testCreateTimeInitiallyNull() {
        DeviceInfo d = new DeviceInfo();
        // createTime由数据库自动设置，实体类初始为null
        assertNull(d.getCreateTime());
    }
}
