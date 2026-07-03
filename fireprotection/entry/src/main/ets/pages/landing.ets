import { prompt, router } from "@kit.ArkUI";
import Preferences from '@ohos.data.preferences';

@Entry
@Component
struct LoginPage {
  @State username: string = '';
  @State password: string = '';

  prefs: Preferences.Preferences | null = null;

  aboutToAppear() {
    this.initPrefs();
  }

  // 初始化本地存储
  async initPrefs() {
    try {
      let ctx = getContext(this);
      this.prefs = await Preferences.getPreferences(ctx, 'login_info');
      this.loadAccount();
    } catch (e) {
      prompt.showToast({ message: '初始化失败' });
    }
  }

  // 加载保存的账号
  async loadAccount() {
    if (!this.prefs) return;

    let user = await this.prefs.get('username', '');
    let pwd = await this.prefs.get('password', '');
    this.username = user as string;
    this.password = pwd as string;
  }

  // 保存账号到本地
  async saveAccount() {
    if (!this.prefs) return;

    await this.prefs.put('username', this.username);
    await this.prefs.put('password', this.password);
    await this.prefs.flush(); // 记得flush，不然可能没保存
  }

  build() {
    Stack() {
      // 背景层
      Column()
        .width('100%')
        .height('100%')
        .linearGradient({
          direction: GradientDirection.Top,
          colors: [['#0D3B0D', 0], ['#1B5E20', 0.4], ['#2E7D32', 0.7], ['#388E3C', 1]]
        })

      // 装饰元素 - 顶部圆形光斑
      Column()
        .width(260)
        .height(260)
        .borderRadius(130)
        .backgroundColor('rgba(255,255,255,0.05)')
        .position({ x: -80, y: -60 })

      Column()
        .width(180)
        .height(180)
        .borderRadius(90)
        .backgroundColor('rgba(255,255,255,0.04)')
        .position({ x: '60%', y: '35%' })

      // 主内容区
      Column() {
        // Logo 区域
        Column({ space: 8 }) {
          Text('🌲')
            .fontSize(52)
          Text('森林慧眼')
            .fontSize(28)
            .fontWeight(FontWeight.Bold)
            .fontColor('#FFFFFF')
            .letterSpacing(3)
          Text('森林防火智能监控系统')
            .fontSize(13)
            .fontColor('rgba(255,255,255,0.7)')
            .letterSpacing(4)
        }
        .margin({ top: 80, bottom: 50 })

        // 登录卡片
        Column({ space: 16 }) {
          Text('账号登录')
            .fontSize(18)
            .fontWeight(FontWeight.Bold)
            .fontColor('#333333')
            .width('100%')

          // 用户名输入
          Row() {
            Text('👤')
              .fontSize(18)
              .margin({ left: 12, right: 8 })
            TextInput({ placeholder: '请输入用户名', text: this.username })
              .onChange((v) => this.username = v)
              .layoutWeight(1)
              .height(48)
              .backgroundColor(Color.Transparent)
              .fontSize(15)
          }
          .width('100%')
          .height(50)
          .backgroundColor('#F5F5F5')
          .borderRadius(12)

          // 密码输入
          Row() {
            Text('🔒')
              .fontSize(18)
              .margin({ left: 12, right: 8 })
            TextInput({ placeholder: '请输入密码', text: this.password })
              .type(InputType.Password)
              .onChange((v) => this.password = v)
              .layoutWeight(1)
              .height(48)
              .backgroundColor(Color.Transparent)
              .fontSize(15)
          }
          .width('100%')
          .height(50)
          .backgroundColor('#F5F5F5')
          .borderRadius(12)

          // 登录按钮
          Button('登  录')
            .width('100%')
            .height(50)
            .fontSize(17)
            .fontWeight(FontWeight.Bold)
            .fontColor('#FFFFFF')
            .backgroundColor('#2E7D32')
            .borderRadius(12)
            .shadow({ radius: 8, color: '#402E7D32', offsetY: 4 })
            .onClick(async () => {
              if (this.username && this.password) {
                await this.saveAccount();
                router.replaceUrl({ url: 'pages/DisplayPage' });
              } else {
                prompt.showToast({ message: '请输入完整登录信息' });
              }
            })

          // 底部提示
          Text('森林慧眼 v1.0  ·  守护每一片绿色')
            .fontSize(11)
            .fontColor('#BDBDBD')
        }
        .width('100%')
        .padding({ left: 24, right: 24, top: 28, bottom: 24 })
        .backgroundColor(Color.White)
        .borderRadius(20)
        .shadow({ radius: 20, color: '#30000000', offsetY: 8 })

        Blank()
      }
      .width('100%')
      .height('100%')
      .padding({ left: 24, right: 24 })
    }
    .width('100%')
    .height('100%')
  }
}