

#include "lwip/netifapi.h"
#include "wifi_hotspot.h"
#include "wifi_hotspot_config.h"
#include "stdlib.h"
#include "uart.h"
#include "lwip/nettool/misc.h"
#include "soc_osal.h"
#include "app_init.h"
#include "cmsis_os2.h"
#include "wifi_device.h"
#include "wifi_event.h"
#include "lwip/sockets.h"
#include "lwip/ip4_addr.h"
#include "wifi/wifi_connect.h"
#include "dht11/dht11.h"
#include "oled/oled.h"
#include "app_main.h"
#include "adc/ldr.h"
#include "hcsr04/hcsr04.h"
#include "led/led.h"
#include "FLAME/flame.h"
#include "烟雾传感器/mq2.h"
#include "CO2/co2.h"
#include "voice/voice.h"

#define WIFI_TASK_STACK_SIZE 0x2000
#define DELAY_TIME_MS 1000  // 1秒检测一次

DHT11_Data_TypeDef DHT11_Data;
CO2_Data_TypeDef CO2_Data;
char LampSt[4] = {0};
int lampState = 1;
int32_t distance;
uint16_t ldr_value;

uint8_t g_flame_detected;
uint8_t g_smog_detected;
uint8_t g_fire_alarm = 0;

static void *environment_task(const char *arg)
{
    unused(arg);
    char lcd_buff[100] = {0};
    errcode_t result;
    osal_msleep(1000);

    while (1)
    {
        // DHT11
        result = dht11_read_data(&DHT11_Data);
        if (result == ERRCODE_SUCC)
        {
            printf("Temp:%d.%d Humi:%d.%d\n",
                   DHT11_Data.temp_high8bit, DHT11_Data.temp_low8bit,
                   DHT11_Data.humi_high8bit, DHT11_Data.humi_low8bit);
            memset(lcd_buff, 0, 100);
            sprintf(lcd_buff, "T:%d.%d H:%d.%d",
                    DHT11_Data.temp_high8bit, DHT11_Data.temp_low8bit,
                    DHT11_Data.humi_high8bit, DHT11_Data.humi_low8bit);
            bsp_oled_DrawString(0, 0, lcd_buff, Font_7x10, White);
        }

        // LDR
        ldr_value = get_adc_value();
        memset(lcd_buff, 0, 100);
        sprintf(lcd_buff, "Lumi:%d", ldr_value);
        bsp_oled_DrawString(0, 10, lcd_buff, Font_7x10, White);
        if (ldr_value > 50) {
            led_on(1); lampState = 0;
        } else {
            led_off(1); lampState = 1;
        }

        // CO2
        result = co2_read_data(&CO2_Data);
        if (1)
        // if (result == ERRCODE_SUCC)      // 先不进行校验，看看数据
        {   
            printf("CO2地址位:%x %x\n", CO2_Data.addr_high8bit, CO2_Data.addr_low8bit);
            printf("CO2原始数据:TVOC:%x %x CH2O:%x %x CO2:%x %x 校验和:%x\n",
                   CO2_Data.TVOC_high8bit, CO2_Data.TVOC_low8bit,
                   CO2_Data.CH2O_high8bit, CO2_Data.CH2O_low8bit,
                   CO2_Data.CO2_high8bit, CO2_Data.CO2_low8bit,
                   CO2_Data.check_sum);
            printf("CO2:%.1f TVOC:%.1f CH2O:%.1f\n", CO2_Data.CO2, CO2_Data.TVOC, CO2_Data.CH2O);
            memset(lcd_buff, 0, 100);
            if (CO2_Data.CO2 > 0)
                sprintf(lcd_buff, "CO2:%d", CO2_Data.check_sum);
            else
                sprintf(lcd_buff, "CO2:---");
            bsp_oled_DrawString(0, 20, lcd_buff, Font_7x10, White);
        }
        // gas_sensor_poll();
        // memset(lcd_buff, 0, 100);
        // if (g_gas.co2 > 0)
        //     sprintf(lcd_buff, "CO2:%.1f", g_gas.co2);
        // else
        //     sprintf(lcd_buff, "CO2:---");
        // bsp_oled_DrawString(0, 20, lcd_buff, Font_7x10, White);

        // 烟雾
        g_smog_detected = mq2_is_detected();
        memset(lcd_buff, 0, 100);
        sprintf(lcd_buff, "Smog:%-6s", g_smog_detected ? "WARN!" : "OK");
        bsp_oled_DrawString(0, 30, lcd_buff, Font_7x10, White);

        // 火焰 (显示实际电平+状态)
        g_flame_detected = flame_is_detected();
        memset(lcd_buff, 0, 100);
        sprintf(lcd_buff, "Fire:%-7s", g_flame_detected ? "ALARM!" : "OK");
        bsp_oled_DrawString(0, 40, lcd_buff, Font_7x10, White);

        // 超声波
        distance = hcsr04_get_distance();
        memset(lcd_buff, 0, 100);
        sprintf(lcd_buff, "Dist:%dmm", (int)distance);
        bsp_oled_DrawString(0, 50, lcd_buff, Font_7x10, White);

        // 火灾报警
        g_fire_alarm = (g_flame_detected || g_smog_detected) ? 1 : 0;
        if (g_fire_alarm) printf(">>> FIRE ALARM! <<<\r\n");

        bsp_oled_UpdateScreen();

        memset(LampSt, 0, 4);
        snprintf_s(LampSt, sizeof(LampSt), sizeof(LampSt) - 1, "%s",
                   lampState == 1 ? "ON" : "OFF");
        osDelay(DELAY_TIME_MS);
    }
    return NULL;
}

static void gpio_init(void) { }

static void environment_sensor_init(void)
{
    printf("=== Sensor Init Begin ===\r\n");
    dht11_init();
    oled_init();
    hcsr04_init();
    adc_init();
    led_init();
    flame_init();
    mq2_init();
    co2_init(9600);
    voice_uart_init();
    printf("=== Sensor Init Done ===\r\n");
}

static void *appmain_start(const char *argument)
{
    unused(argument);
    gpio_init();
    environment_sensor_init();
    wifi_connect();
    return NULL;
}

static void app_main(void)
{
    printf("=== SMART FIRE SYSTEM ===\r\n");
    osal_kthread_lock();
    osal_task *task1 = osal_kthread_create((osal_kthread_handler)appmain_start, 0,
                                            "appmain_start", WIFI_TASK_STACK_SIZE);
    osal_kthread_set_priority(task1, 10);
    osal_task *task2 = osal_kthread_create((osal_kthread_handler)environment_task, 0,
                                            "Environment_task", 0x1000);
    osal_kthread_set_priority(task2, 10);
    osal_task *task3 = osal_kthread_create((osal_kthread_handler)voice_task, 0,
                                            "VoiceTask", 0x1000);
    osal_kthread_set_priority(task3, 12);
    osal_kthread_unlock();
}

app_run(app_main);

