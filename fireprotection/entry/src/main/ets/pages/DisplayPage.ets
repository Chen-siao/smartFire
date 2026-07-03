import { rcp } from '@kit.RemoteCommunicationKit';
import { BusinessError } from '@kit.BasicServicesKit';
import http from '@ohos.net.http';
import promptAction from '@ohos.promptAction';
import { router } from '@kit.ArkUI';
import { common, Want } from '@kit.AbilityKit';

// API 地址
const authUrl = "https://iam.cn-north-4.myhuaweicloud.com/v3/auth/tokens";
const deviceShadowUrl ='https://9f43cb188f.st1.iotda-app.cn-north-4.myhuaweicloud.com:443/v5/iot/a884874443da4964b439a9464c11388e/devices/6a43b0f77f2e6c302f8090c6_202408/shadow';
const commandUrl ='https://9f43cb188f.st1.iotda-app.cn-north-4.myhuaweicloud.com:443/v5/iot/a884874443da4964b439a9464c11388e/devices/6a43b0f77f2e6c302f8090c6_202408/commands';

let headersObject = '', aToken = '', shadowArray = '', reported = '';
let property = '', prints_report = '';

// 请求体数据
const requestBody = `{
      "auth": {
        "identity": {
          "methods": [
            "password"
          ],
          "password": {
            "user": {
              "name": "zgl123",
              "password": "sbsyr158",
              "domain": {
                "name": "xsfyd1"
              }
            }
          }
        },
        "scope": {
          "project": {
            "name": "cn-north-4"
          }
        }
      }
    }`;

const postContent: rcp.RequestContent = requestBody;

@Entry
@Component
struct Display {
  @State temperature: string = '--'
  @State humidity: string = '--'
  @State co2: string  = '--'
  @State smoke: string  = '--'
  @State light: string  = '--'
  @State fire_type: string  = '--'
  @State confidence: string = '--'
  @State alert_level: string  = '--'
  @State alert_history: string  = '--'

  aboutToAppear() {
    this.initData()
  }

  initData() {
    const session = rcp.createSession();
    session.post(authUrl, postContent)
      .then((response) => {
        console.info(`Response succeeded: ${JSON.stringify(response.headers)}`);
        const temp = JSON.stringify(response.headers);
        headersObject = JSON.parse(temp);
        aToken = headersObject['x-subject-token'];
        console.log('解析出的 X-Subject-Token 值为:', aToken);
        this.startTimer()
      })
      .catch((err: BusinessError) => {
        console.error(`Response err: Code is ${JSON.stringify(err.code)}, message is ${JSON.stringify(err)}`);
      })
  }

  startTimer() {
    setInterval(async () => {
      console.log('--- 开始定时获取设备属性 ---');
      await this.fetchDeviceData();
    }, 1000);
  }

  async fetchDeviceData() {
    try {
      const instance = http.createHttp()
      const reqOpt: http.HttpRequestOptions = {
        method: http.RequestMethod.GET,
        header: {
          'Content-type': 'application/json',
          'X-Auth-Token': aToken
        }
      };
      const res = await instance.request(deviceShadowUrl, reqOpt);

      console.log('【调试】原始 res 对象:', JSON.stringify(res, null, 2));
      console.log('【调试】res.result:', res.result);

      const dataall = res.result as string

      console.log(dataall)
      shadowArray = JSON.parse(dataall);
      reported = shadowArray['shadow'][0];
      property = reported['reported'];
      prints_report = property['properties'];

      this.temperature = prints_report['temperature'] ?? '--'
      this.humidity = prints_report['humidity'] ?? '--'
      this.co2 = prints_report['co2'] ?? '--'
      this.smoke = String(prints_report['smoke'] ?? '--')
      this.light = prints_report['light'] ?? '--'
      this.fire_type = prints_report['fire_type'] ?? '--'
      this.confidence = String(prints_report['confidence'] ?? '--')
      this.alert_level = prints_report['alert_level'] ?? '--'
      this.alert_history = prints_report['alert_history']?.length ? JSON.stringify(prints_report['alert_history']) : '无记录'

      console.log('温度(temperature): ' + this.temperature + '°C');
      console.log('湿度(humidity): ' + this.humidity + '%');
      console.log('CO2(co2): ' + this.co2 + 'ppm');
      console.log('烟雾(smoke): ' + this.smoke);
      console.log('光照(light): ' + this.light);
      console.log('火情(fire_type): ' + this.fire_type);
      console.log('置信度(confidence): ' + this.confidence);
      console.log('火情等级(alert_level): ' + this.alert_level);
      console.log('火情历史(alert_history): ' + this.alert_history);

    } catch (error) {
      console.error('获取设备数据失败:', error);
    }


  }

  getAlertLevelText(level: string | number): string {
    switch (String(level)) {
      case '0': return '安全';
      case '1': return '注意';
      case '2': return '警告';
      case '3': return '危险';
      default: return '未知';
    }
  }

  getAlertLevelColor(level: string | number): Color {
    switch (String(level)) {
      case '0': return Color.Green;
      case '1': return Color.Yellow;
      case '2': return Color.Orange;
      case '3': return Color.Red;
      default: return Color.Gray;
    }
  }

  getAlertLevelBgColor(level: string | number): string {
    switch (String(level)) {
      case '3': return '#FFEBEE';
      case '2': return '#FFF3E0';
      case '1': return '#FFFDE7';
      default: return '#E8F5E9';
    }
  }

  getFireTypeText(type: string | number): string {
    switch (String(type)) {
      case '0': return '无火情';
      case '1': return '明火';
      case '2': return '阴燃';
      case '3': return '烟雾';
      default: return '未知';
    }
  }

  dialFireAlarm() {
    try {
      let context = getContext(this) as common.UIAbilityContext;
      // 尝试多种方式唤起拨号
      const attempts: Want[] = [
        { uri: 'tel:119' },
        { action: 'ohos.want.action.dial', uri: 'tel:119' },
        { action: 'android.intent.action.DIAL', uri: 'tel:119' }
      ];

      const tryNext = (index: number) => {
        if (index >= attempts.length) {
          promptAction.showToast({ message: '模拟器暂不支持拨号，请手动拨打119' });
          return;
        }
        context.startAbility(attempts[index]).catch(() => {
          tryNext(index + 1);
        });
      };
      tryNext(0);
    } catch (error) {
      console.error('调起拨号失败:', error);
      promptAction.showToast({ message: '模拟器暂不支持拨号，请手动拨打119' });
    }
  }

  @Builder sensorCard(icon: string, label: string, value: string | number, unit: string, color: string) {
    Column() {
      Row() {
        Text(icon).fontSize(18)
        Text(label).fontSize(13).fontColor('#757575').margin({ left: 6 })
      }.width('100%')

      Row() {
        Text(String(value))
          .fontSize(26)
          .fontWeight(FontWeight.Bold)
          .fontColor(color)
        if (unit != '') {
          Text(unit)
            .fontSize(13)
            .fontColor('#9E9E9E')
            .margin({ top: 8, left: 3 })
        }
      }.width('100%').margin({ top: 8 })
    }
    .width('100%')
    .padding(16)
    .backgroundColor(Color.White)
    .borderRadius(14)
    .shadow({ radius: 6, color: '#10000000', offsetY: 2 })
  }

  build() {
    Column() {
      // 顶部标题栏
      Row() {
        Column() {
          Text('ForestGuard')
            .fontSize(11)
            .fontColor('rgba(255,255,255,0.6)')
            .letterSpacing(3)
          Text('森林慧眼')
            .fontSize(20)
            .fontWeight(FontWeight.Bold)
            .fontColor('#FFFFFF')
        }
        Blank()
        Text('监测中')
          .fontSize(11)
          .fontColor('#2E7D32')
          .padding({ left: 12, right: 12, top: 5, bottom: 5 })
          .backgroundColor('#E8F5E9')
          .borderRadius(12)
          .fontWeight(FontWeight.Medium)
      }
      .width('100%')
      .padding({ left: 20, right: 20, top: 14, bottom: 14 })
      .backgroundColor('#1B5E20')

      Scroll() {
        Column({ space: 16 }) {
          // 火情等级大卡片
          Column() {
            Row() {
              Column() {
                Text(this.getAlertLevelText(this.alert_level) === '危险' ? '🔥' :
                     this.getAlertLevelText(this.alert_level) === '警告' ? '⚠️' :
                     this.getAlertLevelText(this.alert_level) === '注意' ? '🔔' : '✅')
                  .fontSize(40)
              }
              .width(56)
              .height(56)
              .backgroundColor('rgba(255,255,255,0.7)')
              .borderRadius(28)
              .justifyContent(FlexAlign.Center)
              .alignItems(HorizontalAlign.Center)

              Column() {
                Text('当前火情等级')
                  .fontSize(11)
                  .fontColor('#888888')
                Text(this.getAlertLevelText(this.alert_level))
                  .fontSize(22)
                  .fontWeight(FontWeight.Bold)
                  .fontColor(this.getAlertLevelColor(this.alert_level))
                  .margin({ top: 2 })
              }
              .alignItems(HorizontalAlign.Start)
              .margin({ left: 12 })

              Blank()

              Column() {
                Text('火情类型')
                  .fontSize(10)
                  .fontColor('#AAAAAA')
                Text(this.getFireTypeText(this.fire_type))
                  .fontSize(17)
                  .fontWeight(FontWeight.Medium)
                  .fontColor('#555555')
                  .margin({ top: 3 })
                Text('置信度 ' + this.confidence)
                  .fontSize(12)
                  .fontColor('#888888')
                  .margin({ top: 4 })
              }
              .alignItems(HorizontalAlign.End)
            }
            .width('100%')
          }
          .width('100%')
          .padding({ left: 16, right: 16, top: 16, bottom: 16 })
          .backgroundColor(this.getAlertLevelBgColor(this.alert_level))
          .borderRadius(16)
          .border({ width: 2, color: this.getAlertLevelColor(this.alert_level) })

          // 一键报警按钮
          Button() {
            Row() {
              Text('🚨')
                .fontSize(22)
              Text('一键报火警')
                .fontSize(17)
                .fontWeight(FontWeight.Bold)
                .margin({ left: 8 })
            }
            .width('100%')
            .justifyContent(FlexAlign.Center)
          }
          .width('100%')
          .height(50)
          .fontColor('#FFFFFF')
          .backgroundColor('#C62828')
          .borderRadius(14)
          .shadow({ radius: 10, color: '#60C62828', offsetY: 4 })
          .margin({ bottom: 6 })
          .onClick(() => {
            AlertDialog.show({
              title: '确认报火警',
              message: '确认向消防部门发送火灾警报？这将触发紧急响应流程。',
              autoCancel: true,
              alignment: DialogAlignment.Center,
              primaryButton: {
                value: '确认报警',
                fontColor: '#C62828',
                action: () => {
                  this.dialFireAlarm();
                }
              },
              secondaryButton: {
                value: '取消',
                fontColor: '#888888',
                action: () => {}
              }
            });
          })

          // 传感器标题
          Row() {
            Text('传感器数据')
              .fontSize(14)
              .fontWeight(FontWeight.Medium)
              .fontColor('#424242')
          }.width('100%').margin({ top: 6 })

          // 传感器数据
          Column({ space: 12 }) {
            Row() {
              Text('🌡️ 温度')
                .fontSize(15)
                .fontWeight(FontWeight.Medium)
                .width(100)
                .fontColor('#555555')
              Text(this.temperature + '°C')
                .fontSize(17)
                .fontWeight(FontWeight.Bold)
                .fontColor(Number(this.temperature) > 45 ? '#E53935' : Number(this.temperature) > 30 ? '#EF6C00' : '#1B5E20')
            }
            .justifyContent(FlexAlign.Start)
            .width('100%')
            .padding({ left: 16, right: 16, top: 12, bottom: 12 })
            .backgroundColor(Color.White)
            .borderRadius(10)

            Row() {
              Text('💧 湿度')
                .fontSize(15)
                .fontWeight(FontWeight.Medium)
                .width(100)
                .fontColor('#555555')
              Text(this.humidity + '%')
                .fontSize(17)
                .fontWeight(FontWeight.Bold)
                .fontColor(Number(this.humidity) < 30 ? '#E53935' : Number(this.humidity) < 40 ? '#EF6C00' : '#1565C0')
            }
            .justifyContent(FlexAlign.Start)
            .width('100%')
            .padding({ left: 16, right: 16, top: 12, bottom: 12 })
            .backgroundColor(Color.White)
            .borderRadius(10)

            Row() {
              Text('☁️ CO₂浓度')
                .fontSize(15)
                .fontWeight(FontWeight.Medium)
                .width(100)
                .fontColor('#555555')
              Text(this.co2 + 'ppm')
                .fontSize(17)
                .fontWeight(FontWeight.Bold)
                .fontColor(Number(this.co2) > 1000 ? '#E53935' : Number(this.co2) > 600 ? '#EF6C00' : '#555555')
            }
            .justifyContent(FlexAlign.Start)
            .width('100%')
            .padding({ left: 16, right: 16, top: 12, bottom: 12 })
            .backgroundColor(Color.White)
            .borderRadius(10)

            Row() {
              Text('💨 烟雾浓度')
                .fontSize(15)
                .fontWeight(FontWeight.Medium)
                .width(100)
                .fontColor('#555555')
              Text(this.smoke)
                .fontSize(17)
                .fontWeight(FontWeight.Bold)
                .fontColor(Number(this.smoke) > 800 ? '#E53935' : Number(this.smoke) > 400 ? '#EF6C00' : '#555555')
            }
            .justifyContent(FlexAlign.Start)
            .width('100%')
            .padding({ left: 16, right: 16, top: 12, bottom: 12 })
            .backgroundColor(Color.White)
            .borderRadius(10)

            Row() {
              Text('☀️ 光照')
                .fontSize(15)
                .fontWeight(FontWeight.Medium)
                .width(100)
                .fontColor('#555555')
              Text(this.light + 'lux')
                .fontSize(17)
                .fontWeight(FontWeight.Bold)
                .fontColor(Number(this.light) > 80000 ? '#EF6C00' : '#555555')
            }
            .justifyContent(FlexAlign.Start)
            .width('100%')
            .padding({ left: 16, right: 16, top: 12, bottom: 12 })
            .backgroundColor(Color.White)
            .borderRadius(10)

            Row() {
              Text('📊 置信度')
                .fontSize(15)
                .fontWeight(FontWeight.Medium)
                .width(100)
                .fontColor('#555555')
              Text(this.confidence)
                .fontSize(17)
                .fontWeight(FontWeight.Bold)
                .fontColor('#555555')
            }
            .justifyContent(FlexAlign.Start)
            .width('100%')
            .padding({ left: 16, right: 16, top: 12, bottom: 12 })
            .backgroundColor(Color.White)
            .borderRadius(10)
          }
          .width('100%')

          // 火情历史
          Column() {
            Row() {
              Text('📋')
                .fontSize(16)
              Text('火情历史记录')
                .fontSize(14)
                .fontWeight(FontWeight.Medium)
                .fontColor('#424242')
                .margin({ left: 6 })
            }
            .width('100%')
            .margin({ bottom: 10 })

            Text(this.alert_history)
              .fontSize(12)
              .fontColor('#888888')
              .width('100%')
              .lineHeight(20)
              .maxLines(5)
              .textOverflow({ overflow: TextOverflow.Ellipsis })
          }
          .width('100%')
          .padding(16)
          .backgroundColor(Color.White)
          .borderRadius(14)
          .shadow({ radius: 6, color: '#10000000', offsetY: 2 })

          // 查看防火策略按钮
          Button() {
            Row() {
              Text('🛡️')
                .fontSize(18)
              Text('查看防火策略与评判标准')
                .fontSize(15)
                .fontWeight(FontWeight.Medium)
                .margin({ left: 8 })
              Blank()
              Text('›')
                .fontSize(20)
                .fontColor('#FFFFFF')
            }
            .width('100%')
          }
          .width('100%')
          .height(52)
          .backgroundColor('#1B5E20')
          .borderRadius(14)
          .shadow({ radius: 8, color: '#401B5E20', offsetY: 4 })
          .onClick(() => {
            router.pushUrl({ url: 'pages/FireStrategyPage' });
          })
        }
        .width('100%')
        .padding(16)
        .padding({ bottom: 30 })
      }
      .layoutWeight(1)
      .scrollBar(BarState.Off)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#EDF2ED')
  }
}