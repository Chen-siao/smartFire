import { rcp } from '@kit.RemoteCommunicationKit';
import { BusinessError } from '@kit.BasicServicesKit';
import http from '@ohos.net.http'

interface ShadowResponse {
  shadow?: ShadowItem[];
}

interface ShadowItem {
  reported?: ReportedData;
}

interface ReportedData {
  properties?: SensorData;
}

interface SensorData {
  temperature?: number;
  humidity?: number;
  co2?: number;
  smoke?: number;
  light?: number;
  fire_type?: number;
  confidence?: number;
  alert_level?: number;
  alert_history?: string[];
}

const authUrl = "https://iam.cn-north-4.myhuaweicloud.com/v3/auth/tokens";
const deviceShadowUrl =
  'https://9f43cb188f.st1.iotda-app.cn-north-4.myhuaweicloud.com:443/v5/iot/a884874443da4964b439a9464c11388e/devices/6a43b0f77f2e6c302f8090c6_202408/shadow';
let aToken = '';
let shadowResp: ShadowResponse = { shadow: [] };
let shadow: ShadowItem = {} as ShadowItem;
let reported: ReportedData = {} as ReportedData;
let sensorData: SensorData = {} as SensorData;

let temperature: string = '--';
let humidity: string = '--';
let co2: string = '--';
let smoke: string = '--';
let light: string = '--';
let fireType: string = '--';
let confidence: string = '--';
let alertLevel: string = '--';
let alertHistory: string = '无记录';

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

const session = rcp.createSession();

session.post(authUrl, postContent)
  .then((response) => {
    console.info(`Response succeeded: ${JSON.stringify(response.headers)}`);
    console.info(`Response succeeded: ${JSON.stringify(response.statusCode)}`);
    console.info(`Response succeeded: ${JSON.stringify(postContent)}`);
    console.log(JSON.stringify(response.headers)); // ("X-Subject-Token");
    //let res= response.toString()
    const temp = JSON.stringify(response.headers);
    const tokenMatch = temp.match(/"x-subject-token":"([^"]+)"/);
    aToken = tokenMatch ? tokenMatch[1] : '';
    console.log('解析出的 X-Subject-Token 值为:');
    console.log(aToken);

    setInterval(async () => {
      console.log('--- 开始定时获取设备属性 ---');

      const intance = http.createHttp()

        const res = await intance.request(deviceShadowUrl,{
          method:http.RequestMethod.GET,
          header:{
            'Content-type': 'application/json',
            'X-Auth-Token': aToken
          }
        });
        const  dataall =res.result as string


        shadowResp = JSON.parse(dataall) as ShadowResponse;
        const shadowList = shadowResp.shadow ?? [];
        shadow = shadowList.length > 0 ? shadowList[0] : {} as ShadowItem;
        reported = shadow.reported ?? {} as ReportedData;
        sensorData = reported.properties ?? {} as SensorData;

        temperature = sensorData.temperature?.toString() ?? '--';
        humidity = sensorData.humidity?.toString() ?? '--';
        co2 = sensorData.co2?.toString() ?? '--';
        smoke = sensorData.smoke?.toString() ?? '--';
        light = sensorData.light?.toString() ?? '--';
        fireType = sensorData.fire_type?.toString() ?? '--';
        confidence = sensorData.confidence?.toString() ?? '--';
        alertLevel = sensorData.alert_level?.toString() ?? '--';
        alertHistory = sensorData.alert_history?.join(', ') ?? '无记录';

        // 打印各个属性值
        console.log('温度(temperature): ' + temperature + '°C');
        console.log('湿度(humidity): ' + humidity + '%');
        console.log('CO2(co2): ' + co2 + 'ppm');
        console.log('烟雾(smoke): ' + smoke);
        console.log('光照(light): ' + light);
        console.log('火情(fire_type): ' + fireType);
        console.log('置信度(confidence): ' + confidence);
        console.log('火情等级(alert_level): ' + alertLevel);
        console.log('火情历史(alert_history): ' + alertHistory);
    }, 1000); // interval 毫秒后执行第一次任务，并每隔 interval 毫秒重复执行
  })
  .catch((err: BusinessError) => {
    console.error(`Response err: Code is ${JSON.stringify(err.code)}, message is ${JSON.stringify(err)}`);
  })

@Entry
@Component
struct display {
  build() {
    Column() {

    }
  }
}

