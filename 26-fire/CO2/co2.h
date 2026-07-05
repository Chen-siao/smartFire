#ifndef _CO2_H_
#define _CO2_H_

#include "pinctrl.h"
#include "common_def.h"
#include "soc_osal.h"
#include "osal_wait.h"
#include "app_init.h"
#include "gpio.h"
#include "adc.h"
#include "adc_porting.h"
#include "stdio.h"
#include "hal_gpio.h"
#include "hal_timer.h"

// CO2 数据类型定义
typedef struct
{   
    uint8_t addr_high8bit; // 模块地址
    uint8_t addr_low8bit;  // 模块地址
	uint8_t TVOC_high8bit; // 原始数据：TVOC高8位
	uint8_t TVOC_low8bit;  // 原始数据：TVOC低8位
	uint8_t CH2O_high8bit; // 原始数据：CH2O高8位
	uint8_t CH2O_low8bit;  // 原始数据：CH2O低8位
    uint8_t CO2_high8bit;  // 原始数据：CO2高8位
    uint8_t CO2_low8bit;   // 原始数据：CO2低8位
	uint8_t check_sum;	   // 校验和
	float TVOC;		   // 实际TVOC
	float CH2O;		   // 实际CH2O
	float CO2;			   // 实际CO2
} CO2_Data_TypeDef;


// CO2 传感器初始化（软件 UART 接收）
void co2_init(unsigned int baud);
errcode_t co2_read_data(CO2_Data_TypeDef *CO2_Data);

#endif
