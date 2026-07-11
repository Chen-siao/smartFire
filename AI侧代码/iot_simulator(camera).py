import cv2
import json
import time
from paho.mqtt import client as mqtt_client
from paho.mqtt.client import CallbackAPIVersion

# ===================== 华为云IoT配置 =====================
DEVICE_ID = "6a43ba2c7f2e6c302f80931c_SF001"
CLIENT_ID = "6a43ba2c7f2e6c302f80931c_SF001_0_0_2026070312"
USERNAME = "6a43ba2c7f2e6c302f80931c_SF001"
PASSWORD = "10d81a255ffe36d1f149ec699b85d453c439ee5a773a8591053eda4faf79b42f"
HOST = "17f1fc05ef.st1.iotda-device.cn-north-4.myhuaweicloud.com"
PORT = 8883

SERVICE_ID = "smartFire"
PUB_TOPIC = f"$oc/devices/{CLIENT_ID}/sys/properties/report"


# ===================== MQTT连接 =====================
def connect_mqtt():
    def on_connect(client, userdata, flags, rc):
        if rc == 0:
            print("✅ MQTT连接成功")
        else:
            print("❌ MQTT连接失败:", rc)

    client = mqtt_client.Client(
        CallbackAPIVersion.VERSION2,
        CLIENT_ID
    )

    client.username_pw_set(USERNAME, PASSWORD)
    client.tls_set()
    client.on_connect = on_connect
    client.connect(HOST, PORT, 60)
    return client


# ===================== fire_type逻辑（替代YOLO）====================
def classify_fire(frame):
    """
    用图像亮度 + 颜色简单模拟火焰识别
    后面你可以替换成 YOLO
    """
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
    brightness = hsv[..., 2].mean()

    # 简单规则（保证0-3都可能出现）
    if brightness > 180:
        return 0, 0.95   # 无火
    elif brightness > 140:
        return 1, 0.90   # 疑似烟雾
    elif brightness > 90:
        return 2, 0.92   # 小火
    else:
        return 3, 0.93   # 大火


# ===================== 上报 =====================
def publish(client, fire_type, confidence, alert_history):
    payload = {
        "services": [
            {
                "service_id": SERVICE_ID,
                "properties": {
                    "fire_type": int(fire_type),
                    "confidence": round(confidence, 3),
                    "alert_history": int(alert_history)
                }
            }
        ]
    }

    msg = json.dumps(payload, ensure_ascii=False)

    result = client.publish(PUB_TOPIC, msg)

    if result[0] == 0:
        print("📤 上报成功:", msg)
    else:
        print("❌ 上报失败")


# ===================== 主程序（关键修改点）====================
def run():

    client = connect_mqtt()
    client.loop_start()

    cap = cv2.VideoCapture(0, cv2.CAP_DSHOW)

    if not cap.isOpened():
        print("❌ 摄像头打开失败")
        return

    print("🎥 摄像头启动成功（每7秒自动拍照）")

    alert_history = 0
    last_fire_state = 0

    last_time = time.time()

    while True:

        ret, frame = cap.read()
        if not ret:
            print("❌ 读取失败")
            break

        # 显示画面（必须有，否则你会觉得“没动静”）
        cv2.imshow("Fire Camera", frame)

        # 每7秒触发一次识别
        if time.time() - last_time >= 7:

            fire_type, confidence = classify_fire(frame)

            # 🔥 只有发生火情变化或检测到火才计数
            if fire_type > 0 and last_fire_state == 0:
                alert_history += 1

            last_fire_state = fire_type

            publish(client, fire_type, confidence, alert_history)

            print(f"🔥 fire_type={fire_type}, conf={confidence}, alert_history={alert_history}")

            last_time = time.time()

        # 按Q退出
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

        time.sleep(0.05)

    cap.release()
    cv2.destroyAllWindows()
    client.loop_stop()
    client.disconnect()
    print("🔌 已退出")


if __name__ == "__main__":
    run()