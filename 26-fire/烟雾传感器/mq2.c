/*****************************************************************************************/
/*                                                                                       */
/*                  版权所有：沈阳市网联通信规划设计有限公司                                 */
/*                  开发人员：程国辉 刘艳                                                  */
/*                  联系方式：908536420  3512904489                                       */
/*                  文件名称：mq2.c                                                       */
/*                  功能描述：MQ2烟雾传感器驱动实现文件 (WS63平台, 仅数字量)                 */
/*                  开发时间：2026年6月                                                   */
/*                  版本：V1.0                                                           */
/*****************************************************************************************/

#include "mq2.h"

/*
 * 模块: MQ2烟雾传感器驱动 (WS63平台, 仅数字量)
 * 接线: VCC→3.3V, GND→GND, DO→GPIO_10
 * 原理: 检测到可燃气体/烟雾 → DO输出低电平, 正常 → DO输出高电平
 *       可通过模块上的电位器调节检测灵敏度
 *       上电需预热约1~2分钟
 */

uint8_t g_mq2_status = 0;            // 烟雾检测状态 (1=有烟, 0=无烟)
static int g_mq2_init_ok = 0;

//--------------------------------------------------------------------------------------------------
//  初始化 — GPIO_10 上拉输入
//--------------------------------------------------------------------------------------------------
int mq2_init(void)
{
    if (g_mq2_init_ok)
    {
        printf("MQ2 sensor already initialized\r\n");
        return 0;
    }

    uapi_pin_set_mode(MQ2_DO_PIN, MQ2_DO_PIN_MODE);
    uapi_gpio_set_dir(MQ2_DO_PIN, GPIO_DIRECTION_OUTPUT);
    uapi_gpio_set_val(MQ2_DO_PIN, GPIO_LEVEL_HIGH);
    uapi_gpio_set_dir(MQ2_DO_PIN, GPIO_DIRECTION_INPUT);
    uapi_pin_set_pull(MQ2_DO_PIN, PIN_PULL_TYPE_UP);

    g_mq2_init_ok = 1;
    printf("MQ2 smoke sensor init OK (DO=GPIO_%d)\r\n", MQ2_DO_PIN);

    return 0;
}

//--------------------------------------------------------------------------------------------------
//  检测烟雾
//  返回: 1 = 检测到烟雾 (DO低电平), 0 = 正常 (DO高电平)
//--------------------------------------------------------------------------------------------------
uint8_t mq2_is_detected(void)
{
    gpio_level_t level = uapi_gpio_get_val(MQ2_DO_PIN);

    if (level == GPIO_LEVEL_LOW)
    {
        g_mq2_status = 1;
        return 1;
    }
    else
    {
        g_mq2_status = 0;
        return 0;
    }
}

//--------------------------------------------------------------------------------------------------
//  综合检测+打印
//--------------------------------------------------------------------------------------------------
void mq2_check(void)
{
    if (mq2_is_detected())
    {
        printf("Smog: DETECTED! [ALARM]\r\n");
    }
    else
    {
        printf("Smog: OK\r\n");
    }
}
