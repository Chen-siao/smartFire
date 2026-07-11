/*****************************************************************************************/
/*                                                                                       */
/*                  版权所有：沈阳市网联通信规划设计有限公司                                 */
/*                  开发人员：程国辉 刘艳                                                  */
/*                  联系方式：908536420  3512904489                                       */
/*                  文件名称：voice.c                                                     */
/*                  功能描述：语音模块驱动实现 (WS63平台, UART_BUS_2 GPIO_07/08)            */
/*                  开发时间：2026年7月                                                   */
/*                  版本：V1.0                                                           */
/*****************************************************************************************/

#include "voice.h"

/*
 * 模块: 语音模块驱动 (仅火焰+烟雾查询 + AND逻辑自动报警)
 * 接线: 语音模块TX → GPIO_07 (WS63 RX)
 *       语音模块RX → GPIO_08 (WS63 TX)
 * 协议: 115200 8N1, AA 55 [type] [val] 55 AA (6字节)
 */

/* ============ 外部全局变量 (来自app_main.c, 不修改原代码) ============ */
extern uint8_t g_flame_detected;
extern uint8_t g_smog_detected;

/* ============ 内部变量 ============ */
#define VOICE_RECV_SIZE    14
#define VOICE_INT_MODE     1

static uint8_t voice_rx_buf[VOICE_RECV_SIZE] = {0};
static uint8_t voice_tx_packet[13] = {0};       // 最大13字节，烟雾6字节/火焰9字节

#if (VOICE_INT_MODE)
static uint8_t voice_rx_flag = 0;
#endif

static int voice_init_ok = 0;

static uart_buffer_config_t g_voice_uart_buffer = {
    .rx_buffer = NULL,
    .rx_buffer_size = 0
};

/* ============ UART接收回调 ============ */
#if (VOICE_INT_MODE)
static void voice_uart_callback(const void *buffer, uint16_t length, bool error)
{
    unused(error);
    if (buffer == NULL || length == 0 || length > VOICE_RECV_SIZE) {
        return;
    }
    memcpy(voice_rx_buf, buffer, length);
    voice_rx_flag = 1;
}
#endif

/* ============ 初始化语音UART (GPIO_07 RX, GPIO_08 TX) ============ */
int voice_uart_init(void)
{
    if (voice_init_ok) {
        printf("Voice UART already initialized\r\n");
        return 0;
    }

    /* GPIO初始化: GPIO08→TX, GPIO07→RX */
    uapi_pin_set_mode(GPIO_08, PIN_MODE_1);
    uapi_pin_set_mode(GPIO_07, PIN_MODE_1);
    uapi_gpio_set_dir(GPIO_08, GPIO_DIRECTION_OUTPUT);
    uapi_gpio_set_dir(GPIO_07, GPIO_DIRECTION_INPUT);

    /* UART配置 */
    uart_attr_t attr = {
        .baud_rate = VOICE_UART_BAUD,
        .data_bits = UART_DATA_BIT_8,
        .stop_bits = UART_STOP_BIT_1,
        .parity = UART_PARITY_NONE
    };

    uart_pin_config_t pin_config = {
        .tx_pin = S_MGPIO0,
        .rx_pin = S_MGPIO1,
        .cts_pin = PIN_NONE,
        .rts_pin = PIN_NONE
    };

    g_voice_uart_buffer.rx_buffer = voice_rx_buf;
    g_voice_uart_buffer.rx_buffer_size = VOICE_RECV_SIZE;

    uapi_uart_deinit(VOICE_UART_BUS);
    int ret = uapi_uart_init(VOICE_UART_BUS, &pin_config, &attr, NULL, &g_voice_uart_buffer);
    if (ret != 0) {
        printf("Voice UART init failed! ret=%02x\r\n", ret);
        return -1;
    }

    voice_init_ok = 1;
    printf("Voice UART init OK (TX=GPIO_08, RX=GPIO_07, baud=%d)\r\n", VOICE_UART_BAUD);
    return 0;
}

/* ============ 语音指令解析 ============ */
static void voice_cmd_dispatch(uint8_t *info)
{
    memset(voice_tx_packet, 0, 13);
    printf("Voice input: 0x%02x\n", info[0]);

    uint8_t temp = info[0];
    switch (temp) {
        case 0x0D: // 查询烟雾状态 (9字节)
            printf("Voice cmd: Query smoke\r\n");
            voice_tx_packet[0] = 0xAA;
            voice_tx_packet[1] = 0x55;
            voice_tx_packet[2] = 0x01;
            voice_tx_packet[3] = g_smog_detected;
            voice_tx_packet[4] = 0x00;
            voice_tx_packet[5] = 0x00;
            voice_tx_packet[6] = 0x00;
            voice_tx_packet[7] = 0x55;
            voice_tx_packet[8] = 0xAA;
            uapi_uart_write(VOICE_UART_BUS, voice_tx_packet, 9, 0);
            printf("Send smoke: %s\r\n", g_smog_detected ? "SMOG!" : "SAFE");
            break;

        case 0x0E: // 查询火焰状态 (9字节)
            printf("Voice cmd: Query flame\r\n");
            voice_tx_packet[0] = 0xAA;
            voice_tx_packet[1] = 0x55;
            voice_tx_packet[2] = 0x02;
            voice_tx_packet[3] = g_flame_detected;
            voice_tx_packet[4] = 0x00;
            voice_tx_packet[5] = 0x00;
            voice_tx_packet[6] = 0x00;
            voice_tx_packet[7] = 0x55;
            voice_tx_packet[8] = 0xAA;
            uapi_uart_write(VOICE_UART_BUS, voice_tx_packet, 9, 0);
            printf("Send flame: %s\r\n", g_flame_detected ? "FIRE!" : "SAFE");
            break;

        default:
            printf("Unknown voice cmd: 0x%02x\n", temp);
            break;
    }
}

/* ============ 主动火警推送 (火焰=1 且 烟雾=1 才报警) ============ */
static void voice_fire_push(void)
{
    static uint8_t last_push_alarm = 0;
    /* AND逻辑: 火焰和烟雾同时检测到才触发语音报警 */
    uint8_t voice_alarm = (g_flame_detected && g_smog_detected) ? 1 : 0;

    if (voice_alarm != last_push_alarm) {
        memset(voice_tx_packet, 0, 13);
        /* 13字节格式: AA 55 03 [火焰] 00 00 00 [烟雾] 00 00 00 55 AA */
        voice_tx_packet[0]  = 0xAA;
        voice_tx_packet[1]  = 0x55;
        voice_tx_packet[2]  = 0x03;
        voice_tx_packet[3]  = g_flame_detected;   // 火焰状态
        voice_tx_packet[4]  = 0x00;
        voice_tx_packet[5]  = 0x00;
        voice_tx_packet[6]  = 0x00;
        voice_tx_packet[7]  = g_smog_detected;    // 烟雾状态
        voice_tx_packet[8]  = 0x00;
        voice_tx_packet[9]  = 0x00;
        voice_tx_packet[10] = 0x00;
        voice_tx_packet[11] = 0x55;
        voice_tx_packet[12] = 0xAA;
        uapi_uart_write(VOICE_UART_BUS, voice_tx_packet, 13, 0);

        if (voice_alarm) {
            printf(">>> FIRE ALARM! (flame=%d, smoke=%d) <<<\r\n",
                   g_flame_detected, g_smog_detected);
        } else {
            printf("Alarm cleared. (flame=%d, smoke=%d)\r\n",
                   g_flame_detected, g_smog_detected);
        }
        last_push_alarm = voice_alarm;
    }
}

/* ============ 语音处理任务 ============ */
void *voice_task(const char *arg)
{
    unused(arg);

    /* 注册UART接收回调 */
#if (VOICE_INT_MODE)
    if (uapi_uart_register_rx_callback(VOICE_UART_BUS,
                                       1,     /* UART_RX_CONDITION_MASK_IDLE */
                                       1,
                                       voice_uart_callback) != ERRCODE_SUCC) {
        printf("Voice UART rx callback register failed!\r\n");
    }
#endif

    printf("Voice task started.\r\n");

    uint32_t tick = 0;
    while (1) {
#if (VOICE_INT_MODE)
        /* 等待接收完成，同时定时检查火警推送 */
        while (!voice_rx_flag) {
            osal_msleep(10);
            tick++;
            if (tick >= 10) {
                tick = 0;
                voice_fire_push();  /* 每100ms检查一次火警，不阻塞 */
            }
        }
        voice_rx_flag = 0;
        voice_cmd_dispatch(voice_rx_buf);
        memset(voice_rx_buf, 0, VOICE_RECV_SIZE);
#else
        /* 轮询模式 */
        uint16_t read_len = uapi_uart_read(VOICE_UART_BUS, voice_rx_buf, VOICE_RECV_SIZE, 100);
        if (read_len > 0) {
            voice_cmd_dispatch(voice_rx_buf);
            memset(voice_rx_buf, 0, VOICE_RECV_SIZE);
        }
        osal_msleep(10);
        tick++;
        if (tick >= 10) {
            tick = 0;
            voice_fire_push();
        }
#endif
    }

    return NULL;
}
