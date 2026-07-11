

#ifndef _APP_MAIN_H
#define _APP_MAIN_H

/* MQTT服务器配置 — 华为云IoT */
#define SERVER_IP_ADDR          "17f1fc05ef.st1.iotda-device.cn-north-4.myhuaweicloud.com"
#define SERVER_IP_PORT           1883

/* MQTT主题配置 */
#define MQTT_CMDTOPIC_SUB       "$oc/devices/6a43ba2c7f2e6c302f80931c_SF001/sys/commands/#"
#define MQTT_DATATOPIC_PUB      "$oc/devices/6a43ba2c7f2e6c302f80931c_SF001/sys/properties/report"
#define MQTT_CLIENT_RESPONSE    "$oc/devices/6a43ba2c7f2e6c302f80931c_SF001/sys/commands/response/request_id=%s"

#define IOT
/* 认证信息 */
#ifdef IOT
#define CLIENT_ID               "6a43ba2c7f2e6c302f80931c_SF001_0_0_2026070210"
#define DEVICEID                "6a43ba2c7f2e6c302f80931c_SF001"
#define CLIENTPASSWORD          "bf1c3600833f7121c58b9426973993ef2b848297af069e7b23b8dc59e95a5508"
#endif

/* WiFi配置 */
#define CONFIG_WIFI_SSID        "icy7ea"              // WiFi热点账号
#define CONFIG_WIFI_PWD         "2475903382"           // WiFi热点密码

#endif /* _APP_MAIN_H */
