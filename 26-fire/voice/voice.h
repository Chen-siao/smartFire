/*****************************************************************************************/
/*                                                                                       */
/*                  版权所有：沈阳市网联通信规划设计有限公司                                 */
/*                  开发人员：程国辉 刘艳                                                  */
/*                  联系方式：908536420  3512904489                                       */
/*                  文件名称：voice.h                                                     */
/*                  功能描述：语音模块驱动头文件 (WS63平台, UART_BUS_2)                     */
/*                  开发时间：2026年7月                                                   */
/*                  版本：V1.0                                                           */
/*****************************************************************************************/

#ifndef __VOICE_H__
#define __VOICE_H__

#include "common_def.h"
#include "pinctrl.h"
#include "uart.h"
#include "soc_osal.h"
#include "gpio.h"
#include "stdio.h"
#include "hal_gpio.h"

/* ============ UART配置 ============ */
/* 语音模块 UART: TX→GPIO_08, RX→GPIO_07 (UART_BUS_2) */
#define VOICE_UART_BUS            UART_BUS_2
#define VOICE_UART_BAUD           115200
#define VOICE_UART_RECV_SIZE      14

/* 函数声明 */
int voice_uart_init(void);                   // 初始化语音UART
void *voice_task(const char *arg);           // 语音指令处理任务

#endif /* __VOICE_H__ */
