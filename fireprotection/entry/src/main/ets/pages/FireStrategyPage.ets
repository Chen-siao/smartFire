import { router } from "@kit.ArkUI";

// 火灾等级评判标准数据
interface FireLevelStandard {
  level: number;
  name: string;
  color: string;
  bgColor: string;
  criteria: string[];
  icon: string;
}

interface StrategyItem {
  title: string;
  items: string[];
}

const fireLevels: FireLevelStandard[] = [
  {
    level: 0,
    name: '安全',
    color: '#2E7D32',
    bgColor: '#E8F5E9',
    icon: '✅',
    criteria: [
      '温度 < 30°C，湿度 > 40%，无烟雾',
      'CO₂ 浓度 < 500ppm',
      '光照正常，无异常热源信号',
      '置信度 < 30%，无火情特征',
      '所有传感器数据均在安全阈值内'
    ]
  },
  {
    level: 1,
    name: '注意',
    color: '#F9A825',
    bgColor: '#FFFDE7',
    icon: '⚠️',
    criteria: [
      '温度 30~45°C 或湿度 < 40%',
      'CO₂ 浓度 500~800ppm',
      '烟雾浓度略有上升但未超标',
      '置信度 30%~50%，存在轻微异常',
      '需人工巡查确认现场情况'
    ]
  },
  {
    level: 2,
    name: '警告',
    color: '#EF6C00',
    bgColor: '#FFF3E0',
    icon: '🔶',
    criteria: [
      '温度 45~65°C，湿度 < 30%',
      'CO₂ 浓度 800~1500ppm',
      '烟雾浓度明显上升，可见烟雾',
      '置信度 50%~80%，火情特征显著',
      '红外热源检测到异常高温点'
    ]
  },
  {
    level: 3,
    name: '危险',
    color: '#C62828',
    bgColor: '#FFEBEE',
    icon: '🔥',
    criteria: [
      '温度 > 65°C，湿度极低',
      'CO₂ 浓度 > 1500ppm',
      '烟雾浓度严重超标，浓烟滚滚',
      '置信度 > 80%，确认火灾发生',
      '明火或大面积阴燃已形成'
    ]
  }
];

// 防火策略数据
const preventionStrategies: StrategyItem[] = [
  {
    title: '日常预防（安全期）',
    items: [
      '定期清理林区枯枝落叶和易燃物，保持防火隔离带宽度不少于30米',
      '每日安排巡逻人员沿固定路线巡查，重点时段早6点至晚8点',
      '检查消防水源和灭火设备，确保蓄水池水量充足、灭火器在有效期内',
      '对进入林区人员进行防火宣传和安全检查，严禁携带火种',
      '维护监控摄像头和传感器设备，确保数据采集正常'
    ]
  },
  {
    title: '加强巡查（注意期）',
    items: [
      '增加巡逻频次至每日3次以上，覆盖所有高风险区域',
      '通过广播、短信等方式向周边居民发布防火提醒',
      '检查并清理防火隔离带，确保宽度和清洁度达标',
      '启动无人机定时巡航，每2小时进行一次高空热成像扫描',
      '消防队伍进入待命状态，车辆和装备提前准备就绪'
    ]
  },
  {
    title: '应急响应（警告期）',
    items: [
      '立即通知消防部门和林业管理部门，报告具体位置和初步情况',
      '启动应急预案，消防队伍携带装备赶赴现场',
      '封锁相关林区入口，疏散周边游客和无关人员',
      '启用全部消防水源和灭火设备，建立临时指挥中心',
      '利用无人机实时监控火情发展，为灭火决策提供数据支持'
    ]
  },
  {
    title: '紧急扑救（危险期）',
    items: [
      '立即拨打119并向应急管理局报告，启动一级响应',
      '组织全部消防力量进行扑救，优先控制火势蔓延方向',
      '疏散林区周边5公里范围内的居民，设置安全警戒线',
      '请求上级部门调派直升机、消防车等重型装备支援',
      '建立联合指挥部，统一调度消防、林业、医疗等多部门力量',
      '实时评估风向和地形，动态调整灭火方案，确保人员安全'
    ]
  }
];

@Entry
@Component
struct FireStrategyPage {
  @State selectedTab: number = 0;

  build() {
    Column() {
      // 顶部导航栏
      Row() {
        Image($r('sys.media.ohos_ic_public_arrow_left'))
          .width(24)
          .height(24)
          .fillColor('#FFFFFF')
          .onClick(() => {
            router.back();
          })
        Text('防火策略与评判标准')
          .fontSize(18)
          .fontWeight(FontWeight.Bold)
          .fontColor('#FFFFFF')
          .margin({ left: 12 })
        Blank()
      }
      .width('100%')
      .padding({ left: 16, right: 16, top: 14, bottom: 14 })
      .backgroundColor('#1B5E20')

      // Tab切换
      Row() {
        Button('火灾等级评判')
          .fontSize(14)
          .fontWeight(this.selectedTab === 0 ? FontWeight.Bold : FontWeight.Normal)
          .fontColor(this.selectedTab === 0 ? '#FFFFFF' : '#1B5E20')
          .backgroundColor(this.selectedTab === 0 ? '#2E7D32' : '#E8F5E9')
          .borderRadius(20)
          .layoutWeight(1)
          .height(38)
          .onClick(() => { this.selectedTab = 0 })
        Button('防火应对策略')
          .fontSize(14)
          .fontWeight(this.selectedTab === 1 ? FontWeight.Bold : FontWeight.Normal)
          .fontColor(this.selectedTab === 1 ? '#FFFFFF' : '#1B5E20')
          .backgroundColor(this.selectedTab === 1 ? '#2E7D32' : '#E8F5E9')
          .borderRadius(20)
          .layoutWeight(1)
          .height(38)
          .onClick(() => { this.selectedTab = 1 })
      }
      .width('100%')
      .padding({ left: 16, right: 16, top: 12, bottom: 8 })
      .backgroundColor('#EDF2ED')

      if (this.selectedTab === 0) {
        // 火灾等级评判标准
        Scroll() {
          Column({ space: 12 }) {
            Text('森林火灾等级评判标准')
              .fontSize(16)
              .fontWeight(FontWeight.Bold)
              .fontColor('#333333')
              .width('100%')
              .margin({ top: 4 })

            Text('基于温度、湿度、CO₂浓度、烟雾浓度、光照及AI置信度综合判定')
              .fontSize(12)
              .fontColor('#888888')
              .width('100%')

            ForEach(fireLevels, (level: FireLevelStandard) => {
              Column() {
                Row() {
                  Text(level.icon)
                    .fontSize(28)
                  Text(level.name)
                    .fontSize(20)
                    .fontWeight(FontWeight.Bold)
                    .fontColor(level.color)
                    .margin({ left: 8 })
                  Blank()
                }
                .width('100%')

                Divider()
                  .strokeWidth(1)
                  .color(level.color)
                  .opacity(0.3)
                  .margin({ top: 8, bottom: 8 })

                ForEach(level.criteria, (item: string) => {
                  Row() {
                    Circle({ width: 6, height: 6 })
                      .fill(level.color)
                      .margin({ right: 10 })
                    Text(item)
                      .fontSize(13)
                      .fontColor('#555555')
                      .layoutWeight(1)
                  }
                  .width('100%')
                  .margin({ top: 4 })
                })
              }
              .width('100%')
              .padding(16)
              .backgroundColor(level.bgColor)
              .borderRadius(14)
              .border({ width: 1.5, color: level.color, style: BorderStyle.Solid })
            })
          }
          .width('100%')
          .padding(16)
          .padding({ bottom: 30 })
        }
        .layoutWeight(1)
        .scrollBar(BarState.Off)
      } else {
        // 防火应对策略
        Scroll() {
          Column({ space: 14 }) {
            Text('森林防火应对策略')
              .fontSize(16)
              .fontWeight(FontWeight.Bold)
              .fontColor('#333333')
              .width('100%')
              .margin({ top: 4 })

            Text('根据火灾等级采取分级响应措施，科学防火、安全第一')
              .fontSize(12)
              .fontColor('#888888')
              .width('100%')

            ForEach(preventionStrategies, (strategy: StrategyItem, index: number) => {
              Column() {
                Row() {
                  Text((index + 1).toString())
                    .fontSize(14)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FFFFFF')
                    .width(28)
                    .height(28)
                    .backgroundColor(['#2E7D32', '#F9A825', '#EF6C00', '#C62828'][index])
                    .borderRadius(14)
                    .textAlign(TextAlign.Center)
                  Text(strategy.title)
                    .fontSize(16)
                    .fontWeight(FontWeight.Bold)
                    .fontColor(['#2E7D32', '#F9A825', '#EF6C00', '#C62828'][index])
                    .margin({ left: 10 })
                }
                .width('100%')

                Divider()
                  .strokeWidth(1)
                  .color(['#2E7D32', '#F9A825', '#EF6C00', '#C62828'][index])
                  .opacity(0.25)
                  .margin({ top: 10, bottom: 10 })

                ForEach(strategy.items, (item: string) => {
                  Row() {
                    Text('•')
                      .fontSize(16)
                      .fontColor(['#2E7D32', '#F9A825', '#EF6C00', '#C62828'][index])
                      .margin({ right: 8 })
                    Text(item)
                      .fontSize(13)
                      .fontColor('#555555')
                      .layoutWeight(1)
                      .lineHeight(20)
                  }
                  .width('100%')
                  .margin({ top: 6 })
                  .alignItems(VerticalAlign.Top)
                })
              }
              .width('100%')
              .padding(16)
              .backgroundColor(Color.White)
              .borderRadius(14)
              .shadow({ radius: 6, color: '#15000000', offsetY: 2 })
            })
          }
          .width('100%')
          .padding(16)
          .padding({ bottom: 30 })
        }
        .layoutWeight(1)
        .scrollBar(BarState.Off)
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#EDF2ED')
  }
}
