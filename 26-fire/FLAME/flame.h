/*****************************************************************************************/
/*                                                                                       */
/*                  版权所有：沈阳市网联通信规划设计有限公司                                 */
/*                  开发人员：程国辉 刘艳                                                  */
/*                  联系方式：908536420  3512904489                                       */
/*                  文件名称：flame.h                                                     */
/*                  功能描述：火焰传感器驱动头文件 (WS63平台, 仅数字量)                      */
/*                  开发时间：2026年6月                                                   */
/*                  版本：V1.0                                                           */
/*****************************************************************************************/

#ifndef __FLAME_H__
#define __FLAME_H__

#include "common_def.h"
#include "pinctrl.h"
#include "soc_osal.h"
#include "gpio.h"
#include "stdio.h"
#include "hal_gpio.h"

/* ============ 引脚配置 ============ */
/* 火焰传感器 DO → GPIO_05 (数字输出, 有火→低电平, 无火→高电平) */
#define FLAME_DO_PIN              GPIO_13
#define FLAME_DO_PIN_MODE         PIN_MODE_0

/* 函数声明 */
int flame_init(void);                // 初始化 GPIO
uint8_t flame_is_detected(void);     // 检测火焰 (1=有火, 0=无火)
void flame_check(void);              // 综合检测+打印

/* 全局变量 */
extern uint8_t g_flame_status;

#endif /* __FLAME_H__ */
