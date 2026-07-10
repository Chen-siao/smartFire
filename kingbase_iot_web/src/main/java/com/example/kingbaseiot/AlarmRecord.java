package com.example.kingbaseiot;

import java.time.LocalDateTime;

public class AlarmRecord {
    private Long id;
    private String deviceId;
    private String alarmType;
    private Integer alarmLevel;
    private String alarmContent;
    private String handleStatus;
    private String handleRemark;
    private LocalDateTime alarmTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getAlarmType() { return alarmType; }
    public void setAlarmType(String alarmType) { this.alarmType = alarmType; }

    public Integer getAlarmLevel() { return alarmLevel; }
    public void setAlarmLevel(Integer alarmLevel) { this.alarmLevel = alarmLevel; }

    public String getAlarmContent() { return alarmContent; }
    public void setAlarmContent(String alarmContent) { this.alarmContent = alarmContent; }

    public String getHandleStatus() { return handleStatus; }
    public void setHandleStatus(String handleStatus) { this.handleStatus = handleStatus; }

    public String getHandleRemark() { return handleRemark; }
    public void setHandleRemark(String handleRemark) { this.handleRemark = handleRemark; }

    public LocalDateTime getAlarmTime() { return alarmTime; }
    public void setAlarmTime(LocalDateTime alarmTime) { this.alarmTime = alarmTime; }
}
