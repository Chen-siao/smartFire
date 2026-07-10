package com.example.kingbaseiot;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SensorRecord {
    private Long id;
    private String deviceId;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private BigDecimal co2;
    private BigDecimal smoke;
    private BigDecimal light;
    private Integer fireType;
    private Integer alertLevel;
    private BigDecimal dist;
    private BigDecimal confidence;
    private String alertHistory;
    private LocalDateTime reportTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public BigDecimal getTemperature() { return temperature; }
    public void setTemperature(BigDecimal temperature) { this.temperature = temperature; }

    public BigDecimal getHumidity() { return humidity; }
    public void setHumidity(BigDecimal humidity) { this.humidity = humidity; }

    public BigDecimal getCo2() { return co2; }
    public void setCo2(BigDecimal co2) { this.co2 = co2; }

    public BigDecimal getSmoke() { return smoke; }
    public void setSmoke(BigDecimal smoke) { this.smoke = smoke; }

    public BigDecimal getLight() { return light; }
    public void setLight(BigDecimal light) { this.light = light; }

    public Integer getFireType() { return fireType; }
    public void setFireType(Integer fireType) { this.fireType = fireType; }

    public Integer getAlertLevel() { return alertLevel; }
    public void setAlertLevel(Integer alertLevel) { this.alertLevel = alertLevel; }

    public BigDecimal getDist() { return dist; }
    public void setDist(BigDecimal dist) { this.dist = dist; }

    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }

    public String getAlertHistory() { return alertHistory; }
    public void setAlertHistory(String alertHistory) { this.alertHistory = alertHistory; }

    public LocalDateTime getReportTime() { return reportTime; }
    public void setReportTime(LocalDateTime reportTime) { this.reportTime = reportTime; }
}
