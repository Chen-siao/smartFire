/******************************************************************************
 * co2.c — 硬件UART0 (TXO/RXO, 9600bps) + 软件串口(已注释保留)
 * 帧(6字节): 2C | VAL1_H VAL1_L | VAL2_H VAL2_L | CHK
 ******************************************************************************/
#include "pinctrl.h"
#include "common_def.h"
#include "soc_osal.h"
#include "osal_wait.h"
#include "app_init.h"
#include "gpio.h"
#include "uart.h"
#include "stdio.h"
#include "hal_gpio.h"

#include "co2.h"

/* ====== 硬件 UART0 实现 ====== */
#define HDR  0x2C
#define FLEN 6

static CO2_Data_TypeDef s_data;
static volatile uint8_t s_rdy;
static uint8_t s_rx[128];
static uint8_t s_buf[FLEN];
static uint8_t s_idx;
static int    s_ok;
static uart_buffer_config_t s_bc;

static void _parse(uint8_t *f)
{
    uint8_t i; uint16_t sum = 0, raw;
    if (f[0] != HDR) return;
    for (i = 0; i < 5; i++) sum += f[i];
    if ((uint8_t)sum != f[5]) return;

    s_data.addr_high8bit = f[0];
    s_data.addr_low8bit  = 0;
    s_data.TVOC_high8bit = f[1];
    s_data.TVOC_low8bit  = f[2];
    s_data.CH2O_high8bit = 0;
    s_data.CH2O_low8bit  = 0;
    s_data.CO2_high8bit  = f[3];
    s_data.CO2_low8bit   = f[4];
    s_data.check_sum     = f[5];
    raw = ((uint16_t)f[1] << 8) | f[2]; s_data.TVOC = raw * 0.001f;
    raw = ((uint16_t)f[3] << 8) | f[4]; s_data.CO2  = raw * 0.1f;
    s_data.CH2O = 0;
    s_rdy = 1;
}

static void _cb(const void *p, uint16_t len, bool err)
{
    uint8_t *d; uint16_t i;
    if (err || !p || !len) return;
    d = (uint8_t *)p;
    for (i = 0; i < len; i++) {
        if (s_idx == 0 && d[i] != HDR) continue;
        if (s_idx < FLEN) s_buf[s_idx++] = d[i];
        if (s_idx >= FLEN) { s_idx = 0; _parse(s_buf); }
    }
}

void co2_init(unsigned int baud)
{
    uart_attr_t a; uart_pin_config_t p;
    if (s_ok) return;
    if (!baud) baud = 9600;

    s_bc.rx_buffer = s_rx; s_bc.rx_buffer_size = 128;

    uapi_pin_set_mode(GPIO_18, PIN_MODE_1); /* RXO */
    uapi_pin_set_mode(GPIO_17, PIN_MODE_1); /* TXO */

    a.baud_rate = baud;
    a.data_bits = UART_DATA_BIT_8;
    a.stop_bits = UART_STOP_BIT_1;
    a.parity    = UART_PARITY_NONE;

    p.rx_pin  = GPIO_18;
    p.tx_pin  = GPIO_17;
    p.cts_pin = PIN_NONE;
    p.rts_pin = PIN_NONE;

    uapi_uart_deinit(0);
    uapi_uart_init(0, &p, &a, NULL, &s_bc);
    uapi_uart_unregister_rx_callback(0);
    uapi_uart_register_rx_callback(0, 5, 1, _cb);

    s_ok = 1; s_rdy = 0; s_idx = 0;
    printf("CO2 UART0 %ubps OK\r\n", baud);
}

errcode_t co2_read_data(CO2_Data_TypeDef *CO2_Data)
{
    if (!CO2_Data || !s_ok) return ERRCODE_FAIL;
    if (s_rdy) { *CO2_Data = s_data; s_rdy = 0; return ERRCODE_SUCC; }
    return ERRCODE_FAIL;
}
