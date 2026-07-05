/*****************************************************************************************/
/*                                                                                       */
/*                  版权所有：沈阳市网联通信规划设计有限公司                                 */
/*                  开发人员：程国辉 刘艳                                                  */
/*                  联系方式：908536420  3512904489                                       */
/*                  文件名称：flame.c                                                     */
/*                  功能描述：火焰传感器驱动实现文件 (WS63平台, 仅数字量)                    */
/*                  开发时间：2026年6月                                                   */
/*                  版本：V1.0                                                           */
/*****************************************************************************************/

#include "flame.h"

/*
 * 模块: 火焰传感器驱动 (WS63平台, 仅数字量)
 * 接线: VCC→3.3V, GND→GND, DO→GPIO_05
 * 原理: 检测到火焰 → DO输出低电平, 无火焰 → DO输出高电平
 *       可通过模块上的电位器调节检测灵敏度
 *       注意: 避免阳光直射、白炽灯等红外源干扰
 */

uint8_t g_flame_status = 0;          // 火焰检测状态 (1=有火, 0=无火)
static int g_flame_init_ok = 0;

//--------------------------------------------------------------------------------------------------
//  初始化 — GPIO_05 上拉输入
//--------------------------------------------------------------------------------------------------
int flame_init(void)
{
    if (g_flame_init_ok)
    {
        printf("Flame sensor already initialized\r\n");
        return 0;
    }

    uapi_pin_set_mode(FLAME_DO_PIN, FLAME_DO_PIN_MODE);
    uapi_gpio_set_dir(FLAME_DO_PIN, GPIO_DIRECTION_OUTPUT);
    uapi_gpio_set_val(FLAME_DO_PIN, GPIO_LEVEL_HIGH);  // 先拉高
    uapi_gpio_set_dir(FLAME_DO_PIN, GPIO_DIRECTION_INPUT);
    uapi_pin_set_pull(FLAME_DO_PIN, PIN_PULL_TYPE_UP);

    g_flame_init_ok = 1;
    printf("Flame sensor init OK (DO=GPIO_%d)\r\n", FLAME_DO_PIN);

    return 0;
}

//--------------------------------------------------------------------------------------------------
//  检测火焰
//  返回: 1 = 检测到火焰 (DO低电平), 0 = 无火焰 (DO高电平)
//--------------------------------------------------------------------------------------------------
uint8_t flame_is_detected(void)
{
    gpio_level_t level = uapi_gpio_get_val(FLAME_DO_PIN);

    if (level == GPIO_LEVEL_LOW)
    {
        g_flame_status = 1;
        return 1;
    }
    else
    {
        g_flame_status = 0;
        return 0;
    }
}

//--------------------------------------------------------------------------------------------------
//  综合检测+打印
//--------------------------------------------------------------------------------------------------
void flame_check(void)
{
    if (flame_is_detected())
    {
        printf("Flame: DETECTED! [ALARM]\r\n");
    }
    else
    {
        printf("Flame: OK\r\n");
    }
}
