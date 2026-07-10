-- 设备信息表：用于完成设备的增删改查
CREATE TABLE IF NOT EXISTS device_info (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(100) UNIQUE NOT NULL,
    device_name VARCHAR(100),
    location VARCHAR(100),
    status VARCHAR(20),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 传感器数据表：FunctionGraph 会持续写入这里
CREATE TABLE IF NOT EXISTS iot_sensor_record (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(100),
    temperature NUMERIC,
    humidity NUMERIC,
    co2 NUMERIC,
    smoke NUMERIC,
    light NUMERIC,
    fire_type INTEGER,
    alert_level INTEGER,
    dist NUMERIC,
    confidence NUMERIC,
    alert_history TEXT,
    report_time TIMESTAMP
);

-- 报警记录表：用于完成报警的新增、修改、删除、查询
CREATE TABLE IF NOT EXISTS alarm_record (
    id SERIAL PRIMARY KEY,
    device_id VARCHAR(100),
    alarm_type VARCHAR(50),
    alarm_level INTEGER,
    alarm_content TEXT,
    handle_status VARCHAR(20) DEFAULT '未处理',
    handle_remark TEXT,
    alarm_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
