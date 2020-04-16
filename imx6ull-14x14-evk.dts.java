/*
 * Copyright (C) 2016 Freescale Semiconductor, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 */

/dts-v1/;

#include <dt-bindings/input/input.h>
#include "imx6ull.dtsi"

/ {
	model = "Freescale i.MX6 ULL 14x14 EVK Board";             //这个是板载级 设备树，主要配置开发板的 外设
	compatible = "fsl,imx6ull-14x14-evk", "fsl,imx6ull";

	chosen {
		stdout-path = &uart1;                                   //chosen,使用uart1作为stdout
	};

	memory {
		reg = <0x80000000 0x20000000>;                          //内存地址与ddr外设相关
	};

	reserved-memory {
		#address-cells = <1>;
		#size-cells = <1>;
		ranges;

		linux,cma {
			compatible = "shared-dma-pool";
			reusable;
			size = <0x8000000>;
			linux,cma-default;
		};
	};

	backlight {                                              //LCD 屏 背光灯
		compatible = "pwm-backlight";
		pwms = <&pwm1 0 5000000>;                              //对应管脚是 GPIO_8 pwm1
		brightness-levels = <0 4 8 16 32 64 128 255>;          //亮度等级
		default-brightness-level = <6>;                        //默认6
		status = "okay";
	};

	pxp_v4l2 {
		compatible = "fsl,imx6ul-pxp-v4l2", "fsl,imx6sx-pxp-v4l2", "fsl,imx6sl-pxp-v4l2";
		status = "okay";
	};

	regulators {
		compatible = "simple-bus";
		#address-cells = <1>;
		#size-cells = <0>;

		reg_can_3v3: regulator@0 {
			compatible = "regulator-fixed";
			reg = <0>;
			regulator-name = "can-3v3";                          //can 电压3.3V
			regulator-min-microvolt = <3300000>;
			regulator-max-microvolt = <3300000>;
			gpios = <&gpio_spi 3 GPIO_ACTIVE_LOW>;              
		};

		reg_sd1_vmmc: regulator@1 {
			compatible = "regulator-fixed";
			regulator-name = "VSD_3V3";                       //sd card 3.3V
			regulator-min-microvolt = <3300000>;
			regulator-max-microvolt = <3300000>;
			/* gpio = <&gpio1 9 GPIO_ACTIVE_HIGH>; */
			enable-active-high;
		};

		reg_gpio_dvfs: regulator-gpio {
			compatible = "regulator-gpio";
			pinctrl-names = "default";
			pinctrl-0 = <&pinctrl_dvfs>;                      //DVFS
			regulator-min-microvolt = <1300000>;
			regulator-max-microvolt = <1400000>;
			regulator-name = "gpio_dvfs";
			regulator-type = "voltage";
			gpios = <&gpio5 3 GPIO_ACTIVE_HIGH>;
			states = <1300000 0x1 1400000 0x0>;
		};
	};

	leds {
		compatible = "gpio-leds";
		pinctrl-names = "default";
		pinctrl-0 = <&pinctrl_gpio_leds
			     &pinctrl_beep>;

		led1{
			label = "sys-led";                                   //系统灯 
			gpios = <&gpio1 3 GPIO_ACTIVE_LOW>;                  //GPIO1
			linux,default-trigger = "heartbeat";
			default-state = "on";
		};

		beep{
			label = "beep";                                      //蜂鸣器
			gpios = <&gpio5 1 GPIO_ACTIVE_LOW>;                  //GPIO5
			default-state = "off";
		};
	};

	gpio_keys: gpio_keys@0 {
		compatible = "gpio-keys";
		pinctrl-names = "default";
		pinctrl-0 = <&pinctrl_gpio_keys>;
		#address-cells = <1>;
		#size-cells = <0>;
		autorepeat;

		key1@1 {
			label = "USER-KEY1";                                //按键 gpio1
			linux,code = <114>;
			gpios = <&gpio1 18 GPIO_ACTIVE_LOW>;
			gpio-key,wakeup;
		};
	};

	sound {
		compatible = "fsl,imx6ul-evk-wm8960",                //声卡 WM8960
			   "fsl,imx-audio-wm8960";
		model = "wm8960-audio";
		cpu-dai = <&sai2>;                                  //使用中等音质接口2
		audio-codec = <&codec>;                             //解码
		asrc-controller = <&asrc>;                          //异步采样率转换器（ASRC）
		codec-master;
		gpr = <&gpr 4 0x100000 0x100000>;
		/*
                 * hp-det = <hp-det-pin hp-det-polarity>;
		 * hp-det-pin: JD1 JD2  or JD3
		 * hp-det-polarity = 0: hp detect high for headphone
		 * hp-det-polarity = 1: hp detect high for speaker
		 */
		hp-det = <3 0>;
		/* hp-det-gpios = <&gpio5 4 0>;
		   mic-det-gpios = <&gpio5 4 0>; */
		audio-routing =
			"Headphone Jack", "HP_L",
			"Headphone Jack", "HP_R",
			"Ext Spk", "SPK_LP",
			"Ext Spk", "SPK_LN",
			"Ext Spk", "SPK_RP",
			"Ext Spk", "SPK_RN",
			"LINPUT2", "Mic Jack",
			"LINPUT3", "Mic Jack",
			"RINPUT1", "Main MIC",
			"RINPUT2", "Main MIC",
			"Mic Jack", "MICB",
			"Main MIC", "MICB",
			"CPU-Playback", "ASRC-Playback",
			"Playback", "CPU-Playback",
			"ASRC-Capture", "CPU-Capture",
			"CPU-Capture", "Capture";
	};

	spi4 {
		compatible = "spi-gpio";
		pinctrl-names = "default";
		pinctrl-0 = <&pinctrl_spi4>;
		pinctrl-assert-gpios = <&gpio5 8 GPIO_ACTIVE_LOW>;
		status = "disabled";
		gpio-sck = <&gpio5 11 0>;  //sck
		gpio-mosi = <&gpio5 10 0>; //mosi
		cs-gpios = <&gpio5 7 0>;   //cs
		num-chipselects = <1>;
		#address-cells = <1>;
		#size-cells = <0>;

		gpio_spi: gpio_spi@0 {                        //595????
			compatible = "fairchild,74hc595";
			gpio-controller;
			#gpio-cells = <2>;
			reg = <0>;
			registers-number = <1>;
			registers-default = /bits/ 8 <0x57>;
			spi-max-frequency = <100000>;
		};
	};
};

&cpu0 {
	arm-supply = <&reg_arm>;
	soc-supply = <&reg_soc>;
	dc-supply = <&reg_gpio_dvfs>;
};

&clks {
	assigned-clocks = <&clks IMX6UL_CLK_PLL4_AUDIO_DIV>;
	assigned-clock-rates = <722534400>;
};

&csi {                                      //COMOS 传感器
	status = "okay";

	port {
		csi1_ep: endpoint {                    //使用片上csi1
			remote-endpoint = <&ov5640_ep>;      //设备为ov6540
		};
	};
};

&ecspi3 {
        fsl,spi-num-chipselects = <1>;
        cs-gpio = <&gpio1 20 GPIO_ACTIVE_LOW>;//ec-spi3: cs
        pinctrl-names = "default";
        pinctrl-0 = <&pinctrl_ecspi3>;
        status = "okay";

	spidev: icm20608@0 {
	compatible = "alientek,icm20608";          //使用ecspi3 接口的6轴陀螺仪传感器
        spi-max-frequency = <8000000>;
        reg = <0>;
    };
};

&fec1 {                                      //网卡1 LN8720A,使用rmii接口
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_enet1                //接口enet1
		     &pinctrl_fec1_reset>;
	phy-mode = "rmii";
	phy-handle = <&ethphy0>;
	phy-reset-gpios = <&gpio5 7 GPIO_ACTIVE_LOW>;  //复位gpio5_07
	phy-reset-duration = <26>;
	status = "okay";
};

&fec2 {                                    //网卡2 LN8720A,使用rmii接口
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_enet2              //接口enet2
		     &pinctrl_fec2_reset>;
	phy-mode = "rmii";
	phy-handle = <&ethphy1>;                 
	phy-reset-gpios = <&gpio5 8 GPIO_ACTIVE_LOW>; //复位gpio5_08
	phy-reset-duration = <26>;               //复位电平时间
	status = "okay";

	mdio {
		#address-cells = <1>;
		#size-cells = <0>;

		ethphy0: ethernet-phy@2 {
			compatible = "ethernet-phy-ieee802.3-c22";
			reg = <0>;
		};

		ethphy1: ethernet-phy@1 {
			compatible = "ethernet-phy-ieee802.3-c22";
			reg = <1>;
		};
	};
};

&flexcan1 {
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_flexcan1>;
	xceiver-supply = <&reg_can_3v3>;   //can电源3.3V
	status = "okay";
};

&flexcan2 {
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_flexcan2>;
	xceiver-supply = <&reg_can_3v3>;
	status = "disabled";
};

&gpc {
	fsl,cpu_pupscr_sw2iso = <0x1>;
	fsl,cpu_pupscr_sw = <0x0>;
	fsl,cpu_pdnscr_iso2sw = <0x1>;
	fsl,cpu_pdnscr_iso = <0x1>;
	fsl,ldo-bypass = <0>; /* DCDC, ldo-enable */
};

&i2c1 {                             //I2C1 
	clock-frequency = <100000>;
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_i2c1>;
	status = "okay";

	mag3110@0e {
		compatible = "fsl,mag3110";    //MAG3110|高精度3D磁力计
		reg = <0x0e>;
		position = <2>;
	};

	ap3216c@1e {
		compatible = "ap3216c";         //光强传感器
		reg = <0x1e>;
	};
};

&i2c2 {
	clock_frequency = <100000>;
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_i2c2>;
	status = "okay";

	codec: wm8960@1a {
		compatible = "wlf,wm8960";       //wm8960 语音芯片  SAI2 +i2c(codec 编码)
		reg = <0x1a>;
		clocks = <&clks IMX6UL_CLK_SAI2>;
		clock-names = "mclk";
		wlf,shared-lrclk;
	};

	ov5640: ov5640@3c {               //CMOS 摄像头
		compatible = "ovti,ov5640";
		reg = <0x3c>;
		pinctrl-names = "default";
		pinctrl-0 = <&pinctrl_csi1
			     &csi_pwn_rst>;
		clocks = <&clks IMX6UL_CLK_CSI>;
		clock-names = "csi_mclk";
		pwn-gpios = <&gpio1 4 1>;
		rst-gpios = <&gpio1 2 0>;
		csi_id = <0>;
		mclk = <24000000>;
		mclk_source = <0>;
		status = "okay";
		port {
			ov5640_ep: endpoint {
				remote-endpoint = <&csi1_ep>;
			};
		};
	};

        edt-ft5x06@38 { //电容屏控制芯片FT5306
		compatible = "edt,edt-ft5306", "edt,edt-ft5x06";
		pinctrl-names = "default";
		pinctrl-0 = <&ts_int_pin   //ts管脚接口
			     &ts_reset_pin>;     //ts复位管脚

		reg = <0x38>;
		interrupt-parent = <&gpio1>;
		interrupts = <9 0>;
		reset-gpios = <&gpio5 9 GPIO_ACTIVE_LOW>;  //GPIO5_09

		status = "disabled";
	};

        goodix_ts@5d {
                compatible = "goodix,gt9xx";   //gt9xx GT911系列的液晶触摸驱动
                reg = <0x5d>;
                status = "disabled";
                interrupt-parent = <&gpio1>;
                interrupts = <9 0>;
                pinctrl-0 = <&ts_int_pin
			     &ts_reset_pin>;
		goodix,rst-gpio = <&gpio5 9  GPIO_ACTIVE_LOW>;
		goodix,irq-gpio = <&gpio1 9  GPIO_ACTIVE_LOW>;
	};
};

&iomuxc {
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_hog_1>;
	imx6ul-evk {
		pinctrl_hog_1: hoggrp-1 {
			fsl,pins = <                                                          //                                            <mux_reg conf_reg input_reg mux_mode input_val>           
				MX6UL_PAD_UART1_RTS_B__GPIO1_IO19	0x17059 /* SD1 CD */              //#define MX6UL_PAD_UART1_RTS_B__GPIO1_IO19     0x0090 0x031C   0x0000    0x5      0x0
				MX6UL_PAD_GPIO1_IO05__USDHC1_VSELECT	0x17059 /* SD1 VSELECT */
				MX6UL_PAD_GPIO1_IO00__ANATOP_OTG1_ID    0x13058 /* USB_OTG1_ID */
			>;
		};

    //CMOS csi管脚
		pinctrl_csi1: csi1grp {  
			fsl,pins = <
				MX6UL_PAD_CSI_MCLK__CSI_MCLK		0x1b008
				MX6UL_PAD_CSI_PIXCLK__CSI_PIXCLK	0x1b008
				MX6UL_PAD_CSI_VSYNC__CSI_VSYNC		0x1b008
				MX6UL_PAD_CSI_HSYNC__CSI_HSYNC		0x1b008
				MX6UL_PAD_CSI_DATA00__CSI_DATA02	0x1b008
				MX6UL_PAD_CSI_DATA01__CSI_DATA03	0x1b008
				MX6UL_PAD_CSI_DATA02__CSI_DATA04	0x1b008
				MX6UL_PAD_CSI_DATA03__CSI_DATA05	0x1b008
				MX6UL_PAD_CSI_DATA04__CSI_DATA06	0x1b008
				MX6UL_PAD_CSI_DATA05__CSI_DATA07	0x1b008
				MX6UL_PAD_CSI_DATA06__CSI_DATA08	0x1b008
				MX6UL_PAD_CSI_DATA07__CSI_DATA09	0x1b008
			>;
		};

	  //网卡1 管脚
		pinctrl_enet1: enet1grp {
			fsl,pins = <
				MX6UL_PAD_ENET1_RX_EN__ENET1_RX_EN	0x1b0b0
				MX6UL_PAD_ENET1_RX_ER__ENET1_RX_ER	0x1b0b0
				MX6UL_PAD_ENET1_RX_DATA0__ENET1_RDATA00	0x1b0b0
				MX6UL_PAD_ENET1_RX_DATA1__ENET1_RDATA01	0x1b0b0
				MX6UL_PAD_ENET1_TX_EN__ENET1_TX_EN	0x1b0b0
				MX6UL_PAD_ENET1_TX_DATA0__ENET1_TDATA00	0x1b0b0
				MX6UL_PAD_ENET1_TX_DATA1__ENET1_TDATA01	0x1b0b0
				MX6UL_PAD_ENET1_TX_CLK__ENET1_REF_CLK1	0x4001b031
			>;
		};

    //网卡2 管脚
		pinctrl_enet2: enet2grp {
			fsl,pins = <
				MX6UL_PAD_GPIO1_IO07__ENET2_MDC		0x1b0b0
				MX6UL_PAD_GPIO1_IO06__ENET2_MDIO	0x1b0b0
				MX6UL_PAD_ENET2_RX_EN__ENET2_RX_EN	0x1b0b0
				MX6UL_PAD_ENET2_RX_ER__ENET2_RX_ER	0x1b0b0
				MX6UL_PAD_ENET2_RX_DATA0__ENET2_RDATA00	0x1b0b0
				MX6UL_PAD_ENET2_RX_DATA1__ENET2_RDATA01	0x1b0b0
				MX6UL_PAD_ENET2_TX_EN__ENET2_TX_EN	0x1b0b0
				MX6UL_PAD_ENET2_TX_DATA0__ENET2_TDATA00	0x1b0b0
				MX6UL_PAD_ENET2_TX_DATA1__ENET2_TDATA01	0x1b0b0
				MX6UL_PAD_ENET2_TX_CLK__ENET2_REF_CLK2	0x4001b031
			>;
		};

//ecspi3 管脚
                pinctrl_ecspi3: ecspi3grp {
                        fsl,pins = <
                                MX6UL_PAD_UART2_RTS_B__ECSPI3_MISO        0x100b1  /* MISO*/
                                MX6UL_PAD_UART2_CTS_B__ECSPI3_MOSI        0x100b1  /* MOSI*/
                                MX6UL_PAD_UART2_RX_DATA__ECSPI3_SCLK      0x100b1  /* CLK*/
                                MX6UL_PAD_UART2_TX_DATA__GPIO1_IO20       0x100b0  /* CS*/
                        >;
                };

//LED管脚  gpio1_03
		pinctrl_gpio_leds: gpio-leds {
			fsl,pins = <
				MX6UL_PAD_GPIO1_IO03__GPIO1_IO03	0x17059
			>;
		};

//按键 gpio1_18
		pinctrl_gpio_keys: gpio-keys {
			fsl,pins = <
				MX6UL_PAD_UART1_CTS_B__GPIO1_IO18	0x80000000
			>;
		};

//can1 rx,tx管脚
		pinctrl_flexcan1: flexcan1grp{
			fsl,pins = <
				MX6UL_PAD_UART3_RTS_B__FLEXCAN1_RX	0x1b020    //CAN1 rx端口
				MX6UL_PAD_UART3_CTS_B__FLEXCAN1_TX	0x1b020    //CAN1 tx端口
			>;
		};
//can2 rx,tx管脚
		pinctrl_flexcan2: flexcan2grp{
			fsl,pins = <
				MX6UL_PAD_UART2_RTS_B__FLEXCAN2_RX	0x1b020
				MX6UL_PAD_UART2_CTS_B__FLEXCAN2_TX	0x1b020
			>;
		};
//i2c1 管脚
		pinctrl_i2c1: i2c1grp {
			fsl,pins = <
				MX6UL_PAD_UART4_TX_DATA__I2C1_SCL 0x4001b8b0
				MX6UL_PAD_UART4_RX_DATA__I2C1_SDA 0x4001b8b0
			>;
		};
//i2c2管脚
		pinctrl_i2c2: i2c2grp {
			fsl,pins = <
				MX6UL_PAD_UART5_TX_DATA__I2C2_SCL 0x4001b8b0
				MX6UL_PAD_UART5_RX_DATA__I2C2_SDA 0x4001b8b0
			>;
		};
//LCD 管脚  24位数据线管脚
		pinctrl_lcdif_dat: lcdifdatgrp {
			fsl,pins = <
				MX6UL_PAD_LCD_DATA00__LCDIF_DATA00  0x18
				MX6UL_PAD_LCD_DATA01__LCDIF_DATA01  0x18
				MX6UL_PAD_LCD_DATA02__LCDIF_DATA02  0x18
				MX6UL_PAD_LCD_DATA03__LCDIF_DATA03  0x18
				MX6UL_PAD_LCD_DATA04__LCDIF_DATA04  0x18
				MX6UL_PAD_LCD_DATA05__LCDIF_DATA05  0x18
				MX6UL_PAD_LCD_DATA06__LCDIF_DATA06  0x18
				MX6UL_PAD_LCD_DATA07__LCDIF_DATA07  0x18
				MX6UL_PAD_LCD_DATA08__LCDIF_DATA08  0x18
				MX6UL_PAD_LCD_DATA09__LCDIF_DATA09  0x18
				MX6UL_PAD_LCD_DATA10__LCDIF_DATA10  0x18
				MX6UL_PAD_LCD_DATA11__LCDIF_DATA11  0x18
				MX6UL_PAD_LCD_DATA12__LCDIF_DATA12  0x18
				MX6UL_PAD_LCD_DATA13__LCDIF_DATA13  0x18
				MX6UL_PAD_LCD_DATA14__LCDIF_DATA14  0x18
				MX6UL_PAD_LCD_DATA15__LCDIF_DATA15  0x18
				MX6UL_PAD_LCD_DATA16__LCDIF_DATA16  0x18
				MX6UL_PAD_LCD_DATA17__LCDIF_DATA17  0x18
				MX6UL_PAD_LCD_DATA18__LCDIF_DATA18  0x18
				MX6UL_PAD_LCD_DATA19__LCDIF_DATA19  0x18
				MX6UL_PAD_LCD_DATA20__LCDIF_DATA20  0x18
				MX6UL_PAD_LCD_DATA21__LCDIF_DATA21  0x18
				MX6UL_PAD_LCD_DATA22__LCDIF_DATA22  0x18
				MX6UL_PAD_LCD_DATA23__LCDIF_DATA23  0x18
			>;
		};

//LCD 控制信号 管脚
		pinctrl_lcdif_ctrl: lcdifctrlgrp {
			fsl,pins = <
				MX6UL_PAD_LCD_CLK__LCDIF_CLK	    0x18
				MX6UL_PAD_LCD_ENABLE__LCDIF_ENABLE  0x18
				MX6UL_PAD_LCD_HSYNC__LCDIF_HSYNC    0x18
				MX6UL_PAD_LCD_VSYNC__LCDIF_VSYNC    0x18
			>;
		};

//pwm1 输出GPIO1_08 
		pinctrl_pwm1: pwm1grp {
			fsl,pins = <
				MX6UL_PAD_GPIO1_IO08__PWM1_OUT   0x110b0
			>;
		};
//4bits spi接口 管脚(这个spi一次可以传输4bits)
		pinctrl_qspi: qspigrp {
			fsl,pins = <
				MX6UL_PAD_NAND_WP_B__QSPI_A_SCLK      0x70a1
				MX6UL_PAD_NAND_READY_B__QSPI_A_DATA00 0x70a1
				MX6UL_PAD_NAND_CE0_B__QSPI_A_DATA01   0x70a1
				MX6UL_PAD_NAND_CE1_B__QSPI_A_DATA02   0x70a1
				MX6UL_PAD_NAND_CLE__QSPI_A_DATA03     0x70a1
				MX6UL_PAD_NAND_DQS__QSPI_A_SS0_B      0x70a1
			>;
		};

//语音2
		pinctrl_sai2: sai2grp {
			fsl,pins = <
				MX6UL_PAD_JTAG_TDI__SAI2_TX_BCLK	0x17088
				MX6UL_PAD_JTAG_TDO__SAI2_TX_SYNC	0x17088
				MX6UL_PAD_JTAG_TRST_B__SAI2_TX_DATA	0x11088
				MX6UL_PAD_JTAG_TCK__SAI2_RX_DATA	0x11088
				MX6UL_PAD_JTAG_TMS__SAI2_MCLK		0x17088
			>;
		};

//触屏控制 gpio1_1~4
		pinctrl_tsc: tscgrp {
			fsl,pins = <
				MX6UL_PAD_GPIO1_IO01__GPIO1_IO01	0xb0
				MX6UL_PAD_GPIO1_IO02__GPIO1_IO02	0xb0
				MX6UL_PAD_GPIO1_IO03__GPIO1_IO03	0xb0
				MX6UL_PAD_GPIO1_IO04__GPIO1_IO04	0xb0
			>;
		};

//串口 1 tx,rx
		pinctrl_uart1: uart1grp {
			fsl,pins = <
				MX6UL_PAD_UART1_TX_DATA__UART1_DCE_TX 0x1b0b1
				MX6UL_PAD_UART1_RX_DATA__UART1_DCE_RX 0x1b0b1
			>;
		};
//串口 2 tx,rx
		pinctrl_uart2: uart2grp {
			fsl,pins = <
				MX6UL_PAD_UART2_TX_DATA__UART2_DCE_TX	0x1b0b1
				MX6UL_PAD_UART2_RX_DATA__UART2_DCE_RX	0x1b0b1
				MX6UL_PAD_UART3_RX_DATA__UART2_DCE_RTS	0x1b0b1
				MX6UL_PAD_UART3_TX_DATA__UART2_DCE_CTS	0x1b0b1
			>;
		};
//串口 1 dte
		pinctrl_uart2dte: uart2dtegrp {
			fsl,pins = <
				MX6UL_PAD_UART2_TX_DATA__UART2_DTE_RX	0x1b0b1
				MX6UL_PAD_UART2_RX_DATA__UART2_DTE_TX	0x1b0b1
				MX6UL_PAD_UART3_RX_DATA__UART2_DTE_CTS	0x1b0b1
				MX6UL_PAD_UART3_TX_DATA__UART2_DTE_RTS	0x1b0b1
			>;
		};
//串口 3 tx,rx
		pinctrl_uart3: uart3grp {
			fsl,pins = <
				MX6UL_PAD_UART3_RX_DATA__UART3_DCE_RX	0x1b0b1
				MX6UL_PAD_UART3_TX_DATA__UART3_DCE_TX	0x1b0b1
			>;
		};
//USB Host 控制器1
		pinctrl_usdhc1: usdhc1grp {
			fsl,pins = <
				MX6UL_PAD_SD1_CMD__USDHC1_CMD     0x17059
				MX6UL_PAD_SD1_CLK__USDHC1_CLK     0x10071
				MX6UL_PAD_SD1_DATA0__USDHC1_DATA0 0x17059
				MX6UL_PAD_SD1_DATA1__USDHC1_DATA1 0x17059
				MX6UL_PAD_SD1_DATA2__USDHC1_DATA2 0x17059
				MX6UL_PAD_SD1_DATA3__USDHC1_DATA3 0x17059
			>;
		};

//usb host 1,100Mhz控制
		pinctrl_usdhc1_100mhz: usdhc1grp100mhz {
			fsl,pins = <
				MX6UL_PAD_SD1_CMD__USDHC1_CMD     0x170b9
				MX6UL_PAD_SD1_CLK__USDHC1_CLK     0x100b9
				MX6UL_PAD_SD1_DATA0__USDHC1_DATA0 0x170b9
				MX6UL_PAD_SD1_DATA1__USDHC1_DATA1 0x170b9
				MX6UL_PAD_SD1_DATA2__USDHC1_DATA2 0x170b9
				MX6UL_PAD_SD1_DATA3__USDHC1_DATA3 0x170b9
			>;
		};
//usb host 1,200Mhz控制
		pinctrl_usdhc1_200mhz: usdhc1grp200mhz {
			fsl,pins = <
				MX6UL_PAD_SD1_CMD__USDHC1_CMD     0x170f9
				MX6UL_PAD_SD1_CLK__USDHC1_CLK     0x100f9
				MX6UL_PAD_SD1_DATA0__USDHC1_DATA0 0x170f9
				MX6UL_PAD_SD1_DATA1__USDHC1_DATA1 0x170f9
				MX6UL_PAD_SD1_DATA2__USDHC1_DATA2 0x170f9
				MX6UL_PAD_SD1_DATA3__USDHC1_DATA3 0x170f9
			>;
		};
//usb host 2 控制
		pinctrl_usdhc2: usdhc2grp {
			fsl,pins = <
				MX6UL_PAD_NAND_RE_B__USDHC2_CLK     0x10069
				MX6UL_PAD_NAND_WE_B__USDHC2_CMD     0x17059
				MX6UL_PAD_NAND_DATA00__USDHC2_DATA0 0x17059
				MX6UL_PAD_NAND_DATA01__USDHC2_DATA1 0x17059
				MX6UL_PAD_NAND_DATA02__USDHC2_DATA2 0x17059
				MX6UL_PAD_NAND_DATA03__USDHC2_DATA3 0x17059
			>;
		};
//usb host 2 8位传输 管脚
		pinctrl_usdhc2_8bit: usdhc2grp_8bit {
			fsl,pins = <
				MX6UL_PAD_NAND_RE_B__USDHC2_CLK     0x10069
				MX6UL_PAD_NAND_WE_B__USDHC2_CMD     0x17059
				MX6UL_PAD_NAND_DATA00__USDHC2_DATA0 0x17059
				MX6UL_PAD_NAND_DATA01__USDHC2_DATA1 0x17059
				MX6UL_PAD_NAND_DATA02__USDHC2_DATA2 0x17059
				MX6UL_PAD_NAND_DATA03__USDHC2_DATA3 0x17059
				MX6UL_PAD_NAND_DATA04__USDHC2_DATA4 0x17059
				MX6UL_PAD_NAND_DATA05__USDHC2_DATA5 0x17059
				MX6UL_PAD_NAND_DATA06__USDHC2_DATA6 0x17059
				MX6UL_PAD_NAND_DATA07__USDHC2_DATA7 0x17059
			>;
		};
//usb host 2,100Mhz控制
		pinctrl_usdhc2_8bit_100mhz: usdhc2grp_8bit_100mhz {
			fsl,pins = <
				MX6UL_PAD_NAND_RE_B__USDHC2_CLK     0x100b9
				MX6UL_PAD_NAND_WE_B__USDHC2_CMD     0x170b9
				MX6UL_PAD_NAND_DATA00__USDHC2_DATA0 0x170b9
				MX6UL_PAD_NAND_DATA01__USDHC2_DATA1 0x170b9
				MX6UL_PAD_NAND_DATA02__USDHC2_DATA2 0x170b9
				MX6UL_PAD_NAND_DATA03__USDHC2_DATA3 0x170b9
				MX6UL_PAD_NAND_DATA04__USDHC2_DATA4 0x170b9
				MX6UL_PAD_NAND_DATA05__USDHC2_DATA5 0x170b9
				MX6UL_PAD_NAND_DATA06__USDHC2_DATA6 0x170b9
				MX6UL_PAD_NAND_DATA07__USDHC2_DATA7 0x170b9
			>;
		};
//usb host 2,200Mhz控制
		pinctrl_usdhc2_8bit_200mhz: usdhc2grp_8bit_200mhz {
			fsl,pins = <
				MX6UL_PAD_NAND_RE_B__USDHC2_CLK     0x100f9
				MX6UL_PAD_NAND_WE_B__USDHC2_CMD     0x170f9
				MX6UL_PAD_NAND_DATA00__USDHC2_DATA0 0x170f9
				MX6UL_PAD_NAND_DATA01__USDHC2_DATA1 0x170f9
				MX6UL_PAD_NAND_DATA02__USDHC2_DATA2 0x170f9
				MX6UL_PAD_NAND_DATA03__USDHC2_DATA3 0x170f9
				MX6UL_PAD_NAND_DATA04__USDHC2_DATA4 0x170f9
				MX6UL_PAD_NAND_DATA05__USDHC2_DATA5 0x170f9
				MX6UL_PAD_NAND_DATA06__USDHC2_DATA6 0x170f9
				MX6UL_PAD_NAND_DATA07__USDHC2_DATA7 0x170f9
			>;
		};
//看门狗
		pinctrl_wdog: wdoggrp {
			fsl,pins = <
				MX6UL_PAD_LCD_RESET__WDOG1_WDOG_ANY    0x30b0
			>;
		};

//触屏  中断管脚gpio1-09
		ts_int_pin: ts_int_pin_mux {
			fsl,pins = <
				MX6UL_PAD_GPIO1_IO09__GPIO1_IO09	0x18  //电容屏管脚 GPIO1_09
			>;
		};

//CMOS pwn rst
		csi_pwn_rst: csi_pwn_rstgrp {
			fsl,pins = <
				MX6UL_PAD_GPIO1_IO02__GPIO1_IO02	0x10b0
				MX6UL_PAD_GPIO1_IO04__GPIO1_IO04	0x10b0
			>;
		};
	};
};


//SNVS 管脚
&iomuxc_snvs {
	pinctrl-names = "default_snvs";
        pinctrl-0 = <&pinctrl_hog_2>;
        imx6ul-evk {
		pinctrl_hog_2: hoggrp-2 {
                        fsl,pins = <
                                MX6ULL_PAD_SNVS_TAMPER0__GPIO5_IO00      0x80000000
                        >;
                };

		pinctrl_dvfs: dvfsgrp {
                        fsl,pins = <
                                MX6ULL_PAD_SNVS_TAMPER3__GPIO5_IO03      0x79
                        >;
                };
		
		pinctrl_lcdif_reset: lcdifresetgrp {
                        fsl,pins = <
                                /* used for lcd reset */
                                MX6ULL_PAD_SNVS_TAMPER9__GPIO5_IO09  0x18
                        >;
                };

		pinctrl_spi4: spi4grp {
                        fsl,pins = <
                                MX6ULL_PAD_BOOT_MODE0__GPIO5_IO10        0x70a1
                                MX6ULL_PAD_BOOT_MODE1__GPIO5_IO11        0x70a1
                                MX6ULL_PAD_SNVS_TAMPER7__GPIO5_IO07      0x70a1
                                MX6ULL_PAD_SNVS_TAMPER8__GPIO5_IO08      0x80000000
                        >;
                };

		pinctrl_fec1_reset: fec1_resetgrp {
			fsl,pins = <
				MX6ULL_PAD_SNVS_TAMPER7__GPIO5_IO07	0x79
			>;
		};

		pinctrl_fec2_reset: fec2_resetgrp {
			fsl,pins = <
				MX6ULL_PAD_SNVS_TAMPER8__GPIO5_IO08	0x79
			>;
		};

                pinctrl_sai2_hp_det_b: sai2_hp_det_grp {
                        fsl,pins = <
                                MX6ULL_PAD_SNVS_TAMPER4__GPIO5_IO04   0x17059
                        >;
                };

		ts_reset_pin: ts_reset_pin_mux {
			fsl,pins = <
				MX6ULL_PAD_SNVS_TAMPER9__GPIO5_IO09	0x18              //电容屏复位管脚 GPIO5_09
			>;
		};

		pinctrl_beep: beep {
			fsl,pins = <
				MX6ULL_PAD_SNVS_TAMPER1__GPIO5_IO01	0x17059
			>;
		};
        };
};


//LCD 接口参数
&lcdif {
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_lcdif_dat   //数据管脚
		     &pinctrl_lcdif_ctrl>;      //控制管脚
	display = <&display0>;            //使用display0 节点参数
	status = "okay";

        display0: display {
                bits-per-pixel = <16>;//16位/像素 
                bus-width = <24>;     //24位总线

                display-timings {
                        native-mode = <&timing0>;
                        timing0: timing0 {
                        clock-frequency = <35500000>;
                        hactive = <800>; //
                        vactive = <480>; //
                        hfront-porch = <210>;
                        hback-porch = <46>;
                        hsync-len = <20>;
                        vback-porch = <23>;
                        vfront-porch = <22>;
                        vsync-len = <3>;

                        hsync-active = <0>;
                        vsync-active = <0>;
                        de-active = <1>;
                        pixelclk-active = <1>;
                        };
                };
        };
};

&pwm1 {
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_pwm1>;
	status = "okay";
};

&pxp {
	status = "okay";
};

&qspi {
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_qspi>;
	status = "okay";
	ddrsmp=<0>;

	flash0: n25q256a@0 {
		#address-cells = <1>;
		#size-cells = <1>;
		compatible = "micron,n25q256a";
		spi-max-frequency = <29000000>;
		spi-nor,ddr-quad-read-dummy = <6>;
		reg = <0>;
	};
};

&sai2 {
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_sai2
		     &pinctrl_sai2_hp_det_b>;

	assigned-clocks = <&clks IMX6UL_CLK_SAI2_SEL>,
			  <&clks IMX6UL_CLK_SAI2>;
	assigned-clock-parents = <&clks IMX6UL_CLK_PLL4_AUDIO_DIV>;
	assigned-clock-rates = <0>, <11289600>;

	status = "okay";
};

&tsc {
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_tsc>;
	xnur-gpio = <&gpio1 3 GPIO_ACTIVE_LOW>;
	measure-delay-time = <0xffff>;
	pre-charge-time = <0xfff>;
	status = "disabled";
};

&uart1 {
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_uart1>;
	status = "okay";
};

&uart2 {
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_uart2>;
	fsl,uart-has-rtscts;                      //UART2 使用了RTS   CTS 流控线
	/* for DTE mode, add below change */
	/* fsl,dte-mode; */
	/* pinctrl-0 = <&pinctrl_uart2dte>; */
	status = "disabled";
};

&uart3 {
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_uart3>;
	status = "okay";
};

&usbotg1 {
	dr_mode = "otg";
	srp-disable;
	hnp-disable;
	adp-disable;
	status = "okay";
};

&usbotg2 {
	dr_mode = "host";
	disable-over-current;
	status = "okay";
};

&usbphy1 {
	tx-d-cal = <0x5>;
};

&usbphy2 {
	tx-d-cal = <0x5>;
};

&usdhc1 {
	pinctrl-names = "default", "state_100mhz", "state_200mhz";
	pinctrl-0 = <&pinctrl_usdhc1>;
	pinctrl-1 = <&pinctrl_usdhc1_100mhz>;
	pinctrl-2 = <&pinctrl_usdhc1_200mhz>;
	cd-gpios = <&gpio1 19 GPIO_ACTIVE_LOW>;
	keep-power-in-suspend;
	enable-sdio-wakeup;
	vmmc-supply = <&reg_sd1_vmmc>;
	no-1-8-v;
	status = "okay";
};

&usdhc2 {
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_usdhc2>;
	non-removable;
	status = "okay";
};

&wdog1 {
	pinctrl-names = "default";
	pinctrl-0 = <&pinctrl_wdog>;
	fsl,wdog_b;
};
