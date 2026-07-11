import os
import random
import json
import time
from ultralytics import YOLO
from paho.mqtt import client as mqtt_client
from paho.mqtt.client import CallbackAPIVersion

# ===================== 华为云配置 =====================
DEVICE_ID = "6a43ba2c7f2e6c302f80931c_SF001"
CLIENT_ID = "6a43ba2c7f2e6c302f80931c_SF001_0_0_2026070312"
USERNAME = "6a43ba2c7f2e6c302f80931c_SF001"
PASSWORD = "10d81a255ffe36d1f149ec699b85d453c439ee5a773a8591053eda4faf79b42f"
HOST = "17f1fc05ef.st1.iotda-device.cn-north-4.myhuaweicloud.com"
PORT = 8883

PUB_TOPIC = f"$oc/devices/{DEVICE_ID}/sys/properties/report"
SERVICE_ID = "smartFire"

# ===================== 数据集路径 =====================
FIRE_DIR = r"D:\Desktop\ZhiNengWangLian\AI\fire_dataset\fire_images"
NON_FIRE_DIR = r"D:\Desktop\ZhiNengWangLian\AI\fire_dataset\non_fire_images"

# ===================== YOLO模型 =====================
MODEL_PATH = r"D:\Desktop\ZhiNengWangLian\AI\runs\classify\forest_fire_cls_train\weights\best.pt"
model = YOLO(MODEL_PATH)

# ===================== MQTT连接 =====================
def connect_mqtt():

    def on_connect(client, userdata, flags, rc):
        if rc == 0:
            print("✅ MQTT连接成功")
        else:
            print("❌ 连接失败:", rc)

    client = mqtt_client.Client(
        CallbackAPIVersion.VERSION2,
        CLIENT_ID
    )

    client.username_pw_set(USERNAME, PASSWORD)
    client.tls_set()
    client.connect(HOST, PORT, keepalive=60)

    return client

# ===================== 随机图片 =====================
def get_random_image():

    folder = FIRE_DIR if random.random() > 0.5 else NON_FIRE_DIR
    files = os.listdir(folder)

    img = random.choice(files)
    return os.path.join(folder, img)

# ===================== YOLO推理 =====================
def predict():

    img_path = get_random_image()

    result = model(img_path, verbose=False)[0]

    cls = int(result.probs.top1)
    label = result.names[cls]

    return label

# ===================== fire_type（核心修复） =====================
def get_fire_type(label):

    # 无火
    if label == "no_fire":
        return 0

    # 🔥 火情分级（不依赖confidence）
    r = random.random()

    if r < 0.4:
        return 1   # 疑似烟雾
    elif r < 0.75:
        return 2   # 小火
    else:
        return 3   # 大火

# ===================== alert_history（计数器） =====================
def update_history(history, fire_type):

    if fire_type >= 1:
        return history + 1

    return history

# ===================== 上报 =====================
def publish(client, fire_type, history):

    payload = {
        "services": [
            {
                "service_id": SERVICE_ID,
                "properties": {
                    "fire_type": fire_type,
                    "alert_history": history
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

# ===================== 主循环 =====================
def run():

    client = connect_mqtt()
    client.loop_start()

    alert_history = 0

    print("🔥 火灾检测系统启动")

    try:
        while True:

            label = predict()

            fire_type = get_fire_type(label)

            alert_history = update_history(alert_history, fire_type)

            status_map = ["无火", "疑似烟雾", "小火", "大火"]

            print("\n====================")
            print("label:", label)
            print("fire_type:", fire_type, status_map[fire_type])
            print("alert_history:", alert_history)

            publish(client, fire_type, alert_history)

            time.sleep(2)

    except KeyboardInterrupt:
        print("停止运行")

    finally:
        client.loop_stop()
        client.disconnect()
        print("已断开MQTT")

if __name__ == "__main__":
    run()