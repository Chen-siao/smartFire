/*****************************************************************************************/
/*                                                                                       */
/*                  版权所有：沈阳市网联通信规划设计有限公司                                 */
/*                  开发人员：程国辉 刘艳                                                  */
/*                  联系方式：908536420  3512904489                                       */
/*                  文件名称：mq2.h                                                       */
/*                  功能描述：MQ2烟雾传感器驱动头文件 (WS63平台, 仅数字量)                   */
/*                  开发时间：2026年6月                                                   */
/*                  版本：V1.0                                                           */
/*****************************************************************************************/

#ifndef __MQ2_H__
#define __MQ2_H__

#include "common_def.h"
#include "pinctrl.h"
#include "soc_osal.h"
#include "gpio.h"
#include "stdio.h"
#include "hal_gpio.h"

/* ============ 引脚配置 ============ */
/* 烟雾传感器 DO → GPIO_10 (数字输出, 检测到烟雾→低电平, 无烟→高电平) */
#define MQ2_DO_PIN                GPIO_12
#define MQ2_DO_PIN_MODE           PIN_MODE_0

/* 函数声明 */
int mq2_init(void);                  // 初始化 GPIO
uint8_t mq2_is_detected(void);       // 检测烟雾 (1=有烟, 0=无烟)
void mq2_check(void);                // 综合检测+打印

/* 全局变量 */
extern uint8_t g_mq2_status;

#endif /* __MQ2_H__ */
