/*
 * Copyright 2015-2016 Freescale Semiconductor, Inc.
 * Copyright 2017 NXP.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 */

#include <dt-bindings/clock/imx6ul-clock.h>
#include <dt-bindings/gpio/gpio.h>
#include <dt-bindings/interrupt-controller/arm-gic.h>
#include "imx6ull-pinfunc.h"
#include "imx6ull-pinfunc-snvs.h"
#include "skeleton.dtsi"

/ { //设备树的根
    /*别名*/	
	aliases {
		can0 = &flexcan1;   //FlexCAN是一种扩展了CAN总线功能的嵌入式网络架构。由凯特林大学的Juan Pimentel博士设计。它是受到FlexRay和在CAN总线提供更好的确定行为的需求启发。它的重点在于硬件层的冗余和协议层基于时间的优先级通信
		can1 = &flexcan2;
		ethernet0 = &fec1; //Fast Ethernet Channel 快速以太网信道  The Flexible Controller Area Network (FLEXCAN)
		ethernet1 = &fec2;
		gpio0 = &gpio1;
		gpio1 = &gpio2;
		gpio2 = &gpio3;
		gpio3 = &gpio4;
		gpio4 = &gpio5;
		i2c0 = &i2c1;
		i2c1 = &i2c2;
		i2c2 = &i2c3;
		i2c3 = &i2c4;
		mmc0 = &usdhc1;    //MMC卡（MultiMediaCard）缩写，即多媒体卡   //USB主控制器
		mmc1 = &usdhc2;
		serial0 = &uart1;
		serial1 = &uart2;
		serial2 = &uart3;
		serial3 = &uart4;
		serial4 = &uart5;
		serial5 = &uart6;
		serial6 = &uart7;
		serial7 = &uart8;
		spi0 = &ecspi1;    //Enhanced Configurable Serial Peripheral Interface   增强的可配置串行外围接口
		spi1 = &ecspi2;
		spi2 = &ecspi3;
		spi3 = &ecspi4;
		usbphy0 = &usbphy1; //USB PHY负责最底层的信号转换，作用类似于网口的PHY
		usbphy1 = &usbphy2;
	};

	cpus {
		#address-cells = <1>;
		#size-cells = <0>;

		cpu0: cpu@0 {    //label: node-name@unit-address  格式:   标签:节点名称@地址    引入 label 的目的就是为了方便访问节点，可以直接通过&label 来访问这个节点   例如: &cuu0 就代表 cpu@0
			compatible = "arm,cortex-a7";
			device_type = "cpu";
			reg = <0>;
			clock-latency = <61036>; /* two CLK32 periods */ //clock源到时序器件的clk脚的延迟叫做clock latency.
			operating-points = <     //执行点 操作点
				/* kHz	uV */          //频率 - 工作电压
				996000	1275000
				792000	1225000
				528000	1175000
				396000	1025000
				198000	950000
			>;
			fsl,soc-operating-points = <
				/* KHz	uV */
				996000	1175000
				792000	1175000
				528000	1175000
				396000	1175000
				198000	1175000
			>;
			fsl,low-power-run;                    //低功率运行  ./drivers/cpufreq/imx6q-cpufreq.c:	low_power_run_support = of_property_read_bool(np, "fsl,low-power-run");
			clocks = <&clks IMX6UL_CLK_ARM>,      //引用了609行的 clks: ccm@020c4000 {...} 节点 参数 IMX6UL_CLK_ARM 在 dt-bindings/clock/imx6ul-clock.h 中的定义为 #define IMX6UL_CLK_ARM			93
				                                    //clock-names = "arm" 通过name访问这个值
				                                    //arch/arm/mach-imx/busfreq-imx.c 第1173行: arm_clk =devm_clk_get(&pdev->dev,"arm");被引用
				 <&clks IMX6UL_CLK_PLL2_BUS>,
				 <&clks IMX6UL_CLK_PLL2_PFD2>,
				 <&clks IMX6UL_CA7_SECONDARY_SEL>,
				 <&clks IMX6UL_CLK_STEP>,
				 <&clks IMX6UL_CLK_PLL1_SW>,
				 <&clks IMX6UL_CLK_PLL1_SYS>,
				 <&clks IMX6UL_PLL1_BYPASS>,
				 <&clks IMX6UL_CLK_PLL1>,
				 <&clks IMX6UL_PLL1_BYPASS_SRC>,
				 <&clks IMX6UL_CLK_OSC>;
			clock-names = "arm", "pll2_bus",  "pll2_pfd2_396m", "secondary_sel", "step",
				      "pll1_sw", "pll1_sys", "pll1_bypass", "pll1", "pll1_bypass_src", "osc";
		};
	};

	intc: interrupt-controller@00a01000 {  // Global Interrupt Controller (GIC) 全局中断控制器
		compatible = "arm,cortex-a7-gic";
		#interrupt-cells = <3>;              //描述方法 <中断源,中断号,中断优先级>  如ecspi1 的中断描述为 interrupts = <GIC_SPI 31 IRQ_TYPE_LEVEL_HIGH>;    
		interrupt-controller;                //它是一个中断控制器
		reg = <0x00a01000 0x1000>,
		      <0x00a02000 0x100>;
	};

	clocks {                               //时钟源  ==》在 611 行 ，被clks 使用，
		#address-cells = <1>;
		#size-cells = <0>;

		ckil: clock@0 {                     //32.768k ckil   arch/arm/mach-imx/clk-imx6ul.c 中被使用  clks[IMX6UL_CLK_CKIL] = of_clk_get_by_name(ccm_node, "ckil");
			compatible = "fixed-clock";
			reg = <0>;
			#clock-cells = <0>;
			clock-frequency = <32768>;
			clock-output-names = "ckil";
		};

		osc: clock@1 {                    //24M osc    clks[IMX6UL_CLK_OSC] = of_clk_get_by_name(ccm_node, "osc");
			compatible = "fixed-clock";
			reg = <1>;
			#clock-cells = <0>;             //被引用时，使用 0个参数
			clock-frequency = <24000000>;
			clock-output-names = "osc";
		};

		ipp_di0: clock@2 {              // clks[IMX6UL_CLK_IPP_DI0] = of_clk_get_by_name(ccm_node, "ipp_di0");
			compatible = "fixed-clock";
			reg = <2>;
			#clock-cells = <0>;
			clock-frequency = <0>;
			clock-output-names = "ipp_di0";
		};

		ipp_di1: clock@3 {             // clks[IMX6UL_CLK_IPP_DI1] = of_clk_get_by_name(ccm_node, "ipp_di1");
			compatible = "fixed-clock";
			reg = <3>;
			#clock-cells = <0>;
			clock-frequency = <0>;
			clock-output-names = "ipp_di1";
		};
	};

	soc {
		#address-cells = <1>;
		#size-cells = <1>;
		compatible = "simple-bus";
		interrupt-parent = <&gpc>;    //中断父节点 gpc: 通用电源控制器
		ranges;                       //// ranges;  空“ranges”属性的存在意味着子地址空间中的地址将1:1映射到父地址空间。

		busfreq {                    //总线时钟频率，列表
			compatible = "fsl,imx_busfreq";
			clocks = <&clks IMX6UL_CLK_PLL2_PFD2>, <&clks IMX6UL_CLK_PLL2_198M>,
				 <&clks IMX6UL_CLK_PLL2_BUS>, <&clks IMX6UL_CLK_ARM>,
				 <&clks IMX6UL_CLK_PLL3_USB_OTG>, <&clks IMX6UL_CLK_PERIPH>,
				 <&clks IMX6UL_CLK_PERIPH_PRE>, <&clks IMX6UL_CLK_PERIPH_CLK2>,
				 <&clks IMX6UL_CLK_PERIPH_CLK2_SEL>, <&clks IMX6UL_CLK_OSC>,
				 <&clks IMX6UL_CLK_AHB>, <&clks IMX6UL_CLK_AXI>,
				 <&clks IMX6UL_CLK_PERIPH2>, <&clks IMX6UL_CLK_PERIPH2_PRE>,
				 <&clks IMX6UL_CLK_PERIPH2_CLK2>, <&clks IMX6UL_CLK_PERIPH2_CLK2_SEL>,
				 <&clks IMX6UL_CLK_STEP>, <&clks IMX6UL_CLK_MMDC_P0_FAST>, <&clks IMX6UL_PLL1_BYPASS_SRC>,
				 <&clks IMX6UL_PLL1_BYPASS>, <&clks IMX6UL_CLK_PLL1_SYS>, <&clks IMX6UL_CLK_PLL1_SW>,
				 <&clks IMX6UL_CLK_PLL1>;
			clock-names = "pll2_pfd2_396m", "pll2_198m", "pll2_bus", "arm", "pll3_usb_otg",
				      "periph", "periph_pre", "periph_clk2", "periph_clk2_sel", "osc",
				      "ahb", "ocram", "periph2", "periph2_pre", "periph2_clk2", "periph2_clk2_sel",
				      "step", "mmdc", "pll1_bypass_src", "pll1_bypass", "pll1_sys", "pll1_sw", "pll1";
			fsl,max_ddr_freq = <400000000>;  //DDR 时钟
		};

		pmu {                     //电源管理单元
			compatible = "arm,cortex-a7-pmu";
			interrupts = <GIC_SPI 94 IRQ_TYPE_LEVEL_HIGH>;     // #define GIC_SPI 0 // 共享中断  #define GIC_PPI 1 // 每个处理器拥有独立中断     <共享中断 中断号94  优先级高>
			status = "disabled";    //默认关闭
		};

		ocrams: sram@00900000 {             // Low Power Mode  SRAM  OCRAM 芯上ram
			compatible = "fsl,lpm-sram";
			reg = <0x00900000 0x4000>;
		};

		ocrams_ddr: sram@00904000 {         //DDR   
			compatible = "fsl,ddr-lpm-sram";
			reg = <0x00904000 0x1000>;
		};

		ocram: sram@00905000 {             //SRAM  内存映射     
			compatible = "mmio-sram";
			reg = <0x00905000 0x1B000>;
		};

		dma_apbh: dma-apbh@01804000 {    //DMA APBH 总线
			compatible = "fsl,imx6ul-dma-apbh", "fsl,imx28-dma-apbh";
			reg = <0x01804000 0x2000>;
			interrupts = <GIC_SPI 13 IRQ_TYPE_LEVEL_HIGH>,
				     <GIC_SPI 13 IRQ_TYPE_LEVEL_HIGH>,
				     <GIC_SPI 13 IRQ_TYPE_LEVEL_HIGH>,
				     <GIC_SPI 13 IRQ_TYPE_LEVEL_HIGH>;
			interrupt-names = "gpmi0", "gpmi1", "gpmi2", "gpmi3"; //gpmix 中断都是13 ，高优先级  general-purpose media interface（通用媒体接口)
			#dma-cells = <1>;                                     //1个参数
			dma-channels = <4>;                                   //通道数 == 4
			clocks = <&clks IMX6UL_CLK_APBHDMA>;                  //DMA的时钟
		};

		gpmi: gpmi-nand@01806000{                               //nand 接口
			compatible = "fsl,imx6ull-gpmi-nand", "fsl, imx6ul-gpmi-nand";
			#address-cells = <1>;                                //地址 1个 32位
			#size-cells = <1>;                                   //地址长度
			reg = <0x01806000 0x2000>, <0x01808000 0x4000>;      //180600: gpmi ctrl0地址base     1808000 BCH地址
			reg-names = "gpmi-nand", "bch";                      // nand  bch
			interrupts = <GIC_SPI 15 IRQ_TYPE_LEVEL_HIGH>;       // BCH 中断号15
			interrupt-names = "bch";
			clocks = <&clks IMX6UL_CLK_GPMI_IO>,                 //时钟配置             
				 <&clks IMX6UL_CLK_GPMI_APB>,
				 <&clks IMX6UL_CLK_GPMI_BCH>,
				 <&clks IMX6UL_CLK_GPMI_BCH_APB>,
				 <&clks IMX6UL_CLK_PER_BCH>;
			clock-names = "gpmi_io", "gpmi_apb", "gpmi_bch",    //时钟配置的 访问名称
				      "gpmi_bch_apb", "per1_bch";
			dmas = <&dma_apbh 0>;                               //引用dma aphb 总线 通道0    // dmas  dma-names  被./drivers/dma/of_dma.c 调用，用来读取该信息
			dma-names = "rx-tx";                                //./drivers/mtd/nand/gpmi-nand/gpmi-nand.c 中 dma_chan = dma_request_slave_channel(&pdev->dev, "rx-tx"); 被查找使用
			status = "disabled";
		};

		aips1: aips-bus@02000000 {                            //AIPS-1 
			compatible = "fsl,aips-bus", "simple-bus";
			#address-cells = <1>;
			#size-cells = <1>;
			reg = <0x02000000 0x100000>;                        //0x0200 0000 - 0x020f ffff 所有寄存器的地址范围   芯片手册 176页
			ranges;

			spba-bus@02000000 {                                //AIPS-1 -region(SPBA) SPBA段
				compatible = "fsl,spba-bus", "simple-bus";
				#address-cells = <1>;
				#size-cells = <1>;
				reg = <0x02000000 0x40000>;                      //0200 0000 -0203_FFFF        
				ranges;

				spdif: spdif@02004000 {                          //AIPS-1 -region(SPBA) -SPDIF   0200_4000 0200_7FFF SPDIF 16 KB  // 索尼/飞利浦数字接口（SPDIF）音频块是立体声收发器，它允许处理器以接收和发送数字音频。
					compatible = "fsl,imx6ul-spdif", "fsl,imx35-spdif"; //驱动在./sound/soc/fsl/fsl_spdif.c
					reg = <0x02004000 0x4000>;
					interrupts = <GIC_SPI 52 IRQ_TYPE_LEVEL_HIGH>; //Chapter 50  Sony/Philips Digital Interface (SPDIF) 中断号52
					dmas = <&sdma 41 18 0>,                        //SDMA event mapping (continued) 定义了sdma号  41 42 ??
					       <&sdma 42 18 0>;              
					dma-names = "rx", "tx";
					clocks = <&clks IMX6UL_CLK_SPDIF_GCLK>,
						 <&clks IMX6UL_CLK_OSC>,
						 <&clks IMX6UL_CLK_SPDIF>,
						 <&clks IMX6UL_CLK_DUMMY>, <&clks IMX6UL_CLK_DUMMY>, <&clks IMX6UL_CLK_DUMMY>,
						 <&clks IMX6UL_CLK_IPG>,
						 <&clks IMX6UL_CLK_DUMMY>, <&clks IMX6UL_CLK_DUMMY>,
						 <&clks IMX6UL_CLK_SPBA>;
					clock-names = "core", "rxtx0",
						      "rxtx1", "rxtx2",
						      "rxtx3", "rxtx4",
						      "rxtx5", "rxtx6",
						      "rxtx7", "dma";
					status = "disabled";
				};

				ecspi1: ecspi@02008000 {                       //EC SPI1
					#address-cells = <1>;
					#size-cells = <0>;
					compatible = "fsl,imx6ul-ecspi", "fsl,imx51-ecspi";
					reg = <0x02008000 0x4000>;
					interrupts = <GIC_SPI 31 IRQ_TYPE_LEVEL_HIGH>;
					clocks = <&clks IMX6UL_CLK_ECSPI1>,         //两组时钟： ipg_clk_per ecspi_clk_root eCSPI模块时钟     ipg_clk_s ipg_clk_root外围访问时钟
						 <&clks IMX6UL_CLK_ECSPI1>;
					clock-names = "ipg", "per";
					dmas = <&sdma 3 7 1>, <&sdma 4 7 2>;
					dma-names = "rx", "tx";
					status = "disabled";
				};

				ecspi2: ecspi@0200c000 {
					#address-cells = <1>;
					#size-cells = <0>;
					compatible = "fsl,imx6ul-ecspi", "fsl,imx51-ecspi";
					reg = <0x0200c000 0x4000>;
					interrupts = <GIC_SPI 32 IRQ_TYPE_LEVEL_HIGH>;
					clocks = <&clks IMX6UL_CLK_ECSPI2>,
						 <&clks IMX6UL_CLK_ECSPI2>;
					clock-names = "ipg", "per";
					dmas = <&sdma 5 7 1>, <&sdma 6 7 2>;
					dma-names = "rx", "tx";
					status = "disabled";
				};

				ecspi3: ecspi@02010000 {
					#address-cells = <1>;
					#size-cells = <0>;
					compatible = "fsl,imx6ul-ecspi", "fsl,imx51-ecspi";
					reg = <0x02010000 0x4000>;
					interrupts = <GIC_SPI 33 IRQ_TYPE_LEVEL_HIGH>;
					clocks = <&clks IMX6UL_CLK_ECSPI3>,
						 <&clks IMX6UL_CLK_ECSPI3>;
					clock-names = "ipg", "per";
					dmas = <&sdma 7 7 1>, <&sdma 8 7 2>;
					dma-names = "rx", "tx";
					status = "disabled";
				};

				ecspi4: ecspi@02014000 {
					#address-cells = <1>;
					#size-cells = <0>;
					compatible = "fsl,imx6ul-ecspi", "fsl,imx51-ecspi";
					reg = <0x02014000 0x4000>;
					interrupts = <GIC_SPI 34 IRQ_TYPE_LEVEL_HIGH>;
					clocks = <&clks IMX6UL_CLK_ECSPI4>,
						 <&clks IMX6UL_CLK_ECSPI4>;
					clock-names = "ipg", "per";
					dmas = <&sdma 9 7 1>, <&sdma 10 7 2>;
					dma-names = "rx", "tx";
					status = "disabled";
				};

				uart7: serial@02018000 {
					compatible = "fsl,imx6ul-uart",
						     "fsl,imx6q-uart", "fsl,imx21-uart";
					reg = <0x02018000 0x4000>;
					interrupts = <GIC_SPI 39 IRQ_TYPE_LEVEL_HIGH>;
					clocks = <&clks IMX6UL_CLK_UART7_IPG>,
						 <&clks IMX6UL_CLK_UART7_SERIAL>;
					clock-names = "ipg", "per";
					dmas = <&sdma 43 4 0>, <&sdma 44 4 0>;
					dma-names = "rx", "tx";
					status = "disabled";
				};

				uart1: serial@02020000 {
					compatible = "fsl,imx6ul-uart",
						     "fsl,imx6q-uart", "fsl,imx21-uart";
					reg = <0x02020000 0x4000>;
					interrupts = <GIC_SPI 26 IRQ_TYPE_LEVEL_HIGH>;
					clocks = <&clks IMX6UL_CLK_UART1_IPG>,
						 <&clks IMX6UL_CLK_UART1_SERIAL>;
					clock-names = "ipg", "per";
					status = "disabled";
				};

				esai: esai@02024000 {                                   // 增强型串行音频接口（ESAI）
					compatible = "fsl,imx6ull-esai";
					reg = <0x02024000 0x4000>;
					interrupts = <GIC_SPI 51 IRQ_TYPE_LEVEL_HIGH>;
					clocks = <&clks IMX6UL_CLK_ESAI_IPG>,
						 <&clks IMX6UL_CLK_ESAI_MEM>,
						 <&clks IMX6UL_CLK_ESAI_EXTAL>,
						 <&clks IMX6UL_CLK_ESAI_IPG>,
						 <&clks IMX6UL_CLK_SPBA>;
					clock-names = "core", "mem", "extal",
						      "fsys", "dma";
					dmas = <&sdma 0 21 0>, <&sdma 47 21 0>;
					dma-names = "rx", "tx";
					dma-source = <&gpr 0 14 0 15>;                       // IOMUXC  IO复用配置
					status = "disabled";
				};

				sai1: sai@02028000 {                                   //同步音频接口（SAI）
					compatible = "fsl,imx6ul-sai",
						     "fsl,imx6sx-sai";
					reg = <0x02028000 0x4000>;
					interrupts = <GIC_SPI 97 IRQ_TYPE_LEVEL_HIGH>;
					clocks = <&clks IMX6UL_CLK_SAI1_IPG>,
						 <&clks IMX6UL_CLK_DUMMY>,
						 <&clks IMX6UL_CLK_SAI1>,
						 <&clks 0>, <&clks 0>;
					clock-names = "bus", "mclk0", "mclk1", "mclk2", "mclk3";
					dma-names = "rx", "tx";
					dmas = <&sdma 35 24 0>, <&sdma 36 24 0>;
					status = "disabled";
				};

				sai2: sai@0202c000 {
					compatible = "fsl,imx6ul-sai",
						     "fsl,imx6sx-sai";
					reg = <0x0202c000 0x4000>;
					interrupts = <GIC_SPI 98 IRQ_TYPE_LEVEL_HIGH>;
					clocks = <&clks IMX6UL_CLK_SAI2_IPG>,
						 <&clks IMX6UL_CLK_DUMMY>,
						 <&clks IMX6UL_CLK_SAI2>,
						 <&clks 0>, <&clks 0>;
					clock-names = "bus", "mclk0", "mclk1", "mclk2", "mclk3";
					dma-names = "rx", "tx";
					dmas = <&sdma 37 24 0>, <&sdma 38 24 0>;
					status = "disabled";
				};

				sai3: sai@02030000 {
					compatible = "fsl,imx6ul-sai",
						     "fsl,imx6sx-sai";
					reg = <0x02030000 0x4000>;
					interrupts = <GIC_SPI 24 IRQ_TYPE_LEVEL_HIGH>;
					clocks = <&clks IMX6UL_CLK_SAI3_IPG>,
						 <&clks IMX6UL_CLK_DUMMY>,
						 <&clks IMX6UL_CLK_SAI3>,
						 <&clks 0>, <&clks 0>;
					clock-names = "bus", "mclk0", "mclk1", "mclk2", "mclk3";
					dma-names = "rx", "tx";
					dmas = <&sdma 39 24 0>, <&sdma 40 24 0>;
					status = "disabled";
				};

				asrc: asrc@02034000 {                              // 异步采样率转换器（ASRC）
					compatible = "fsl,imx53-asrc";
					reg = <0x02034000 0x4000>;
					interrupts = <GIC_SPI 50 IRQ_TYPE_LEVEL_HIGH>;
					clocks = <&clks IMX6UL_CLK_ASRC_IPG>,
						<&clks IMX6UL_CLK_ASRC_MEM>, <&clks 0>,
						<&clks 0>, <&clks 0>, <&clks 0>, <&clks 0>,
						<&clks 0>, <&clks 0>, <&clks 0>, <&clks 0>,
						<&clks 0>, <&clks 0>, <&clks 0>, <&clks 0>,
						<&clks IMX6UL_CLK_SPDIF>, <&clks 0>, <&clks 0>,
						<&clks IMX6UL_CLK_SPBA>;
					clock-names = "mem", "ipg", "asrck_0",
						"asrck_1", "asrck_2", "asrck_3", "asrck_4",
						"asrck_5", "asrck_6", "asrck_7", "asrck_8",
						"asrck_9", "asrck_a", "asrck_b", "asrck_c",
						"asrck_d", "asrck_e", "asrck_f", "dma";
					dmas = <&sdma 17 23 1>, <&sdma 18 23 1>, <&sdma 19 23 1>,
						<&sdma 20 23 1>, <&sdma 21 23 1>, <&sdma 22 23 1>;
					dma-names = "rxa", "rxb", "rxc",                //3个通道集
						    "txa", "txb", "txc";
					fsl,asrc-rate  = <48000>;                       //采样率
					fsl,asrc-width = <16>;                          //采样宽度16位
					status = "okay";
				};
			};

			tsc: tsc@02040000 {                                 //触屏
				compatible = "fsl,imx6ul-tsc";
				reg = <0x02040000 0x4000>, <0x0219c000 0x4000>;   //两组寄存器,1组是tsc  2组是ADC
				interrupts = <GIC_SPI 3 IRQ_TYPE_LEVEL_HIGH>,     //TSC 中断
					     <GIC_SPI 101 IRQ_TYPE_LEVEL_HIGH>;         //ADC2中断
				clocks = <&clks IMX6UL_CLK_IPG>,
					 <&clks IMX6UL_CLK_ADC2>;
				clock-names = "tsc", "adc";
				status = "disabled";
			};

			pwm1: pwm@02080000 {                               //PWM
				compatible = "fsl,imx6ul-pwm", "fsl,imx27-pwm";
				reg = <0x02080000 0x4000>;
				interrupts = <GIC_SPI 83 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_PWM1>,
					 <&clks IMX6UL_CLK_PWM1>;
				clock-names = "ipg", "per";
				#pwm-cells = <2>;                               //引用pwm1 时，使用2个参数
			};

			pwm2: pwm@02084000 {
				compatible = "fsl,imx6ul-pwm", "fsl,imx27-pwm";
				reg = <0x02084000 0x4000>;
				interrupts = <GIC_SPI 84 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_DUMMY>,
					 <&clks IMX6UL_CLK_DUMMY>;
				clock-names = "ipg", "per";
				#pwm-cells = <2>;
			};

			pwm3: pwm@02088000 {
				compatible = "fsl,imx6ul-pwm", "fsl,imx27-pwm";
				reg = <0x02088000 0x4000>;
				interrupts = <GIC_SPI 85 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_PWM3>,
					 <&clks IMX6UL_CLK_PWM3>;
				clock-names = "ipg", "per";
				#pwm-cells = <2>;
			};

			pwm4: pwm@0208c000 {
				compatible = "fsl,imx6ul-pwm", "fsl,imx27-pwm";
				reg = <0x0208c000 0x4000>;
				interrupts = <GIC_SPI 86 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_DUMMY>,
					 <&clks IMX6UL_CLK_DUMMY>;
				clock-names = "ipg", "per";
				#pwm-cells = <2>;
			};

			flexcan1: can@02090000 {                                   //flex CAN总线S
				compatible = "fsl,imx6ul-flexcan", "fsl,imx6q-flexcan";
				reg = <0x02090000 0x4000>;
				interrupts = <GIC_SPI 110 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_CAN1_IPG>,
					 <&clks IMX6UL_CLK_CAN1_SERIAL>;
				clock-names = "ipg", "per";
				stop-mode = <&gpr 0x10 1 0x10 17>;
				status = "disabled";
			};

			flexcan2: can@02094000 {
				compatible = "fsl,imx6ul-flexcan", "fsl,imx6q-flexcan";
				reg = <0x02094000 0x4000>;
				interrupts = <GIC_SPI 111 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_CAN2_IPG>,
					 <&clks IMX6UL_CLK_CAN2_SERIAL>;
				clock-names = "ipg", "per";
				stop-mode = <&gpr 0x10 2 0x10 18>;
				status = "disabled";
			};

			gpt1: gpt@02098000 {                                     //通用定时器 General Purpose Timer (GPT) 
				compatible = "fsl,imx6ul-gpt", "fsl,imx31-gpt";
				reg = <0x02098000 0x4000>;
				interrupts = <GIC_SPI 55 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_GPT1_BUS>,
					 <&clks IMX6UL_CLK_GPT_3M>;
				clock-names = "ipg", "osc_per";
			};

			gpio1: gpio@0209c000 {
				compatible = "fsl,imx6ul-gpio", "fsl,imx35-gpio";     //GPIO1 
				reg = <0x0209c000 0x4000>;
				interrupts = <GIC_SPI 66 IRQ_TYPE_LEVEL_HIGH>,        //Combined interrupt indication for GPIO1 signal 0 throughout 15
					     <GIC_SPI 67 IRQ_TYPE_LEVEL_HIGH>;              //Combined interrupt indication for GPIO1 signal 16 throughout 31
				gpio-controller;
				#gpio-cells = <2>;
				interrupt-controller;
				#interrupt-cells = <2>;
			};

			gpio2: gpio@020a0000 {
				compatible = "fsl,imx6ul-gpio", "fsl,imx35-gpio";
				reg = <0x020a0000 0x4000>;
				interrupts = <GIC_SPI 68 IRQ_TYPE_LEVEL_HIGH>,
					     <GIC_SPI 69 IRQ_TYPE_LEVEL_HIGH>;
				gpio-controller;
				#gpio-cells = <2>;
				interrupt-controller;
				#interrupt-cells = <2>;
			};

			gpio3: gpio@020a4000 {
				compatible = "fsl,imx6ul-gpio", "fsl,imx35-gpio";
				reg = <0x020a4000 0x4000>;
				interrupts = <GIC_SPI 70 IRQ_TYPE_LEVEL_HIGH>,
					     <GIC_SPI 71 IRQ_TYPE_LEVEL_HIGH>;
				gpio-controller;
				#gpio-cells = <2>;
				interrupt-controller;
				#interrupt-cells = <2>;
			};

			gpio4: gpio@020a8000 {
				compatible = "fsl,imx6ul-gpio", "fsl,imx35-gpio";
				reg = <0x020a8000 0x4000>;
				interrupts = <GIC_SPI 72 IRQ_TYPE_LEVEL_HIGH>,
					     <GIC_SPI 73 IRQ_TYPE_LEVEL_HIGH>;
				gpio-controller;
				#gpio-cells = <2>;
				interrupt-controller;
				#interrupt-cells = <2>;
			};

			gpio5: gpio@020ac000 {
				compatible = "fsl,imx6ul-gpio", "fsl,imx35-gpio";
				reg = <0x020ac000 0x4000>;
				interrupts = <GIC_SPI 74 IRQ_TYPE_LEVEL_HIGH>,
					     <GIC_SPI 75 IRQ_TYPE_LEVEL_HIGH>;
				gpio-controller;
				#gpio-cells = <2>;
				interrupt-controller;
				#interrupt-cells = <2>;
			};

			snvslp: snvs@020b0000 {                                     //安全的非易失性存储（SNVS）
				compatible = "fsl,imx6ul-snvs";
				reg = <0x020b0000 0x4000>;
				interrupts = <GIC_SPI 4 IRQ_TYPE_LEVEL_HIGH>;
			};

			fec2: ethernet@020b4000 {                                  //网卡2
				compatible = "fsl,imx6ul-fec", "fsl,imx6q-fec";
				reg = <0x020b4000 0x4000>;
				interrupts = <GIC_SPI 120 IRQ_TYPE_LEVEL_HIGH>,         //MAC 0 Periodic Timer Overflow
																																/*MAC 0 Time Stamp Available
																																MAC 0 Payload Receive Error
																																MAC 0 Transmit FIFO Underrun
																																MAC 0 Collision Retry Limit
																																MAC 0 Late Collision
																																MAC 0 Ethernet Bus Error
																																MAC 0 MII Data Transfer Done
																																MAC 0 Receive Buffer Done
																																MAC 0 Receive Frame Done
																																MAC 0 Transmit Buffer Done
																																MAC 0 Transmit Frame Done
																																MAC 0 Graceful Stop
																																MAC 0 Babbling Transmit Error
																																MAC 0 Babbling Receive Error
																																MAC 0 Wakeup Request (sync)*/
					     <GIC_SPI 121 IRQ_TYPE_LEVEL_HIGH>;               //MAC 0 1588 Timer Interrupt – synchronous
				clocks = <&clks IMX6UL_CLK_ENET>,
					 <&clks IMX6UL_CLK_ENET_AHB>,
					 <&clks IMX6UL_CLK_ENET_PTP>,
					 <&clks IMX6UL_CLK_ENET2_REF_125M>,
					 <&clks IMX6UL_CLK_ENET2_REF_125M>;
				clock-names = "ipg", "ahb", "ptp",
					      "enet_clk_ref", "enet_out";
				stop-mode = <&gpr 0x10 4>;
				fsl,num-tx-queues=<1>;                                 //发送队列个数
				fsl,num-rx-queues=<1>;                                 //接收队列个数
				fsl,magic-packet;                                      //支持从网络唤醒
				fsl,wakeup_irq = <0>;                                 
				status = "disabled";
			};

			kpp: kpp@020b8000 {
				compatible = "fsl,imx6ul-kpp", "fsl,imx21-kpp";        //Keypad Port (KPP) 键盘接口  8*8
				reg = <0x020b8000 0x4000>;
				interrupts = <GIC_SPI 82 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_DUMMY>;
				status = "disabled";
			};

			wdog1: wdog@020bc000 {
				compatible = "fsl,imx6ul-wdt", "fsl,imx21-wdt";        //看门狗1
				reg = <0x020bc000 0x4000>;
				interrupts = <GIC_SPI 80 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_WDOG1>;
			};

			wdog2: wdog@020c0000 {
				compatible = "fsl,imx6ul-wdt", "fsl,imx21-wdt";
				reg = <0x020c0000 0x4000>;
				interrupts = <GIC_SPI 81 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_WDOG2>;
				status = "disabled";
			};

			clks: ccm@020c4000 {                                     // ccm,时钟控制器
				compatible = "fsl,imx6ul-ccm";
				reg = <0x020c4000 0x4000>;
				interrupts = <GIC_SPI 87 IRQ_TYPE_LEVEL_HIGH>,
					     <GIC_SPI 88 IRQ_TYPE_LEVEL_HIGH>;
				#clock-cells = <1>;                                   //被其他节点引用时，必须传入 1 个参数 如 clocks = <&clks IMX6UL_CLK_WDOG1>;
				clocks = <&ckil>, <&osc>, <&ipp_di0>, <&ipp_di1>;     //&osc 引用的参数个数是 0
				clock-names = "ckil", "osc", "ipp_di0", "ipp_di1";    //引用访问
			};

			anatop: anatop@020c8000 {                               //模拟电压控制器
				compatible = "fsl,imx6ul-anatop", "fsl,imx6q-anatop",
					     "syscon", "simple-bus";
				reg = <0x020c8000 0x1000>;                           //ANATOP寄存器 20C_8000 20C_8FFF ANALOG_DIG 16 KB  
				                                                     //20C_8000 +  ARM PLL: {0h000, 0h004, 0h008, 0h00C}
				                                                     //20C_8000 +  USB1 PLL: {0h010, 0h014, 0h018, 0h01C}, {0h0F0, 0h0F4, 0h0F8, 0h0FC}
				                                                     //20C_8000 +  System PLL: {0h030, 0h034, 0h038, 0h03C}, 0h040, 0h050, 0h060, {0h100, 0h104,0h108, 0h10C}.
				                                                     //20C_8000 +  Audio / Video PLL: {0h070, 0h074, 0h078, 0h07C}, 0h080, 0h090, {0h0A0, 0h0A4,0h0A8, 0h0AC}, 0h0B0, 0h0C0 
				                                                     //20C_8000 -----20C_817F  : 都是一些CCM_ANALOG时钟配置
				                                                     //温度传感器 20C_8180 - 20C_819F
				                                                     //USB Analog Memory Map  20C_81A0 - 20C_8263  //USB VBUS 电压检测
				interrupts = <GIC_SPI 49 IRQ_TYPE_LEVEL_HIGH>,       //tempsensor   温度传感器  中断
					     <GIC_SPI 54 IRQ_TYPE_LEVEL_HIGH>,             //1.1、2.5或3.0稳压器上的掉电事件。Power Management Unit (PMU)电源管理单元（PMU）。
					     <GIC_SPI 127 IRQ_TYPE_LEVEL_HIGH>;            //内核，GPU或SOC稳压器的掉电事件

				reg_3p0: regulator-3p0@120 {                         //Regulator 3P0 Register (PMU_REG_3P0n) Address: 20C_8000h base + 120h offset + (4d × i), where i=0d to 3d
					                                                   //该寄存器定义了由主机供电的3.0V稳压器的控制位和状态位 USB VBUS引脚。          
					compatible = "fsl,anatop-regulator";               
					regulator-name = "vdd3p0";                         // 3V电压
					regulator-min-microvolt = <2625000>;               //调节器 最小2.625V
					regulator-max-microvolt = <3400000>;               //调节器 最大3.4V
					anatop-reg-offset = <0x120>;                       //120h 偏移地址
					anatop-vol-bit-shift = <8>;                        //电压输出位的开始位 bit8,因为bit8-bit12 为电压输出控制位
					anatop-vol-bit-width = <5>;                       //PMU_REG_3P0n 8-12位为电压输出位OUTPUT_TRG ，每一位表示25mV :例如: 0x00 2.625V  0x0f 3.000V 0x1f 3.400V
					anatop-min-bit-val = <0>;                         
					anatop-min-voltage = <2625000>;
					anatop-max-voltage = <3400000>;
					anatop-enable-bit = <0>;
				};

				reg_arm: regulator-vddcore@140 {                    //Digital Regulator Core Register(PMU_REG_COREn) 地址 20C_8000h base + 140h  有两个电压:cpu电压(bit0-bit4) ，soc电压（bit18-bit22）
					compatible = "fsl,anatop-regulator";
					regulator-name = "cpu";                           //CPU工作电压
					regulator-min-microvolt = <725000>;               //最小电压0.725V
					regulator-max-microvolt = <1450000>;              //最大电压1.45V
					regulator-always-on;                              //常开
					anatop-reg-offset = <0x140>;                      //140h
					anatop-vol-bit-shift = <0>;                       //开始位bit0, bit0-bit4是cpu电压控制位
					anatop-vol-bit-width = <5>;                       //位长度5, bit0-bit4
					anatop-delay-reg-offset = <0x170>;                //Miscellaneous Control Register (PMU_MISC2n)  杂项控制寄存器（PMU_MISC2n) 用来控制一些延时 + 170h
					anatop-delay-bit-shift = <24>;                    //REG0_STEP_TIME bit24-bit25    Number of clock periods (24MHz clock).  00 64_CLOCKS — 64  01 128_CLOCKS — 128  10 256_CLOCKS — 256  11 512_CLOCKS — 512
					anatop-delay-bit-width = <2>;
					anatop-min-bit-val = <1>;
					anatop-min-voltage = <725000>;
					anatop-max-voltage = <1450000>;
				};

				reg_soc: regulator-vddsoc@140 {                     //Digital Regulator Core Register(PMU_REG_COREn) 地址 20C_8000h base + 140h  有两个电压:cpu电压(bit0-bit4) ，soc电压（bit18-bit22）
					compatible = "fsl,anatop-regulator";
					regulator-name = "vddsoc";                        //SOC电压
					regulator-min-microvolt = <725000>;               //最小0.725V
					regulator-max-microvolt = <1450000>;              //最大1.45V
					regulator-always-on;
					anatop-reg-offset = <0x140>;
					anatop-vol-bit-shift = <18>;                     //开始位bit18,            bit18-bit22是cpu电压控制位
					anatop-vol-bit-width = <5>;                      //位长度5,5位表示电压     bit18-bit22是cpu电压控制位
					anatop-delay-reg-offset = <0x170>;               //Miscellaneous Control Register (PMU_MISC2n)  杂项控制寄存器（PMU_MISC2n) 用来控制一些延时 + 170h
					anatop-delay-bit-shift = <28>;                   //bit28 -bit29                  Number of clock periods (24MHz clock).  00 64_CLOCKS — 64  01 128_CLOCKS — 128  10 256_CLOCKS — 256  11 512_CLOCKS — 512
					anatop-delay-bit-width = <2>;
					anatop-min-bit-val = <1>;
					anatop-min-voltage = <725000>;
					anatop-max-voltage = <1450000>;
				};
			};

			usbphy1: usbphy@020c9000 {                           //usb phy(usb网卡1) The UTM provides a 16-bit interface to the USB controller. This interface is clocked at30 MHz.(使用UTMI接口，16位)
				compatible = "fsl,imx6ul-usbphy", "fsl,imx23-usbphy";
				reg = <0x020c9000 0x1000>;                        //基址+长度
				interrupts = <GIC_SPI 44 IRQ_TYPE_LEVEL_HIGH>;    //44号共享中断
				clocks = <&clks IMX6UL_CLK_USBPHY1>;              //时钟
				phy-3p0-supply = <&reg_3p0>;                      //使用3p0提供电源控制
				fsl,anatop = <&anatop>;
			};

			usbphy2: usbphy@020ca000 {                          //usb 网卡 2
				compatible = "fsl,imx6ul-usbphy", "fsl,imx23-usbphy";
				reg = <0x020ca000 0x1000>;
				interrupts = <GIC_SPI 45 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_USBPHY2>;
				phy-3p0-supply = <&reg_3p0>;
				fsl,anatop = <&anatop>;
			};

			tempmon: tempmon {                                  ////温度传感器 20C_8180 - 20C_819F ,地址已经包含在  anatop                     
				compatible = "fsl,imx6ul-tempmon", "fsl,imx6sx-tempmon";
				interrupts = <GIC_SPI 49 IRQ_TYPE_LEVEL_HIGH>;    //49号中断
				fsl,tempmon = <&anatop>;                          //使用anntop配置
				fsl,tempmon-data = <&ocotp>;                      //它使用了oc-otp 字节来配置
				clocks = <&clks IMX6UL_CLK_PLL3_USB_OTG>;
			};

			snvs: snvs@020cc000 {                               //Secure Non-Volatile Storage (SNVS) 安全的非易失性存储（SNVS）
				compatible = "fsl,sec-v4.0-mon", "syscon", "simple-mfd";
				reg = <0x020cc000 0x4000>;

				snvs_rtc: snvs-rtc-lp {                           //RTC:使用纽扣电池保持 
					compatible = "fsl,sec-v4.0-mon-rtc-lp";
					regmap = <&snvs>;
					offset = <0x34>;
					interrupts = <GIC_SPI 19 IRQ_TYPE_LEVEL_HIGH>, <GIC_SPI 20 IRQ_TYPE_LEVEL_HIGH>; //SRTC合并中断。 非TZ。    //SRTC安全中断。 TZ
				};

				snvs_poweroff: snvs-poweroff {                   //poweroff,断电
					compatible = "syscon-poweroff";
					regmap = <&snvs>;
					offset = <0x38>;
					mask = <0x61>;
				};

				snvs_pwrkey: snvs-powerkey {                    //powerkey,电源按键 按下ON-OFF按钮的时间少于5秒（脉冲事件）
					compatible = "fsl,sec-v4.0-pwrkey";
					regmap = <&snvs>;
					interrupts = <GIC_SPI 4 IRQ_TYPE_LEVEL_HIGH>;
					linux,keycode = <KEY_POWER>;
					wakeup;
				};
			};

			epit1: epit@020d0000 {                           //Enhanced Periodic Interrupt Timer (EPIT) 增强型周期性中断定时器（EPIT）
				reg = <0x020d0000 0x4000>;
				interrupts = <GIC_SPI 56 IRQ_TYPE_LEVEL_HIGH>;
			};

			epit2: epit@020d4000 {
				reg = <0x020d4000 0x4000>;
				interrupts = <GIC_SPI 57 IRQ_TYPE_LEVEL_HIGH>;
			};

			src: src@020d8000 {                           //System Reset Controller (SRC) 系统复位控制器
				compatible = "fsl,imx6ul-src", "fsl,imx51-src";
				reg = <0x020d8000 0x4000>;
				interrupts = <GIC_SPI 91 IRQ_TYPE_LEVEL_HIGH>,//SRC interrupt request
					     <GIC_SPI 96 IRQ_TYPE_LEVEL_HIGH>;      //Combined CPU wdog interrupts (4x) out of SRC.(组合式CPU wdog中断（4x）来自SRC。)
				#reset-cells = <1>;
			};

			gpc: gpc@020dc000 {                         //General Power Controller (GPC) 通用电源控制器                
				compatible = "fsl,imx6ul-gpc", "fsl,imx6q-gpc";
				reg = <0x020dc000 0x4000>;
				interrupt-controller;                     //它是一个中断控制器
				#interrupt-cells = <3>;
				interrupts = <GIC_SPI 89 IRQ_TYPE_LEVEL_HIGH>;  //89号共享中断
				interrupt-parent = <&intc>;                     //父中断
				fsl,mf-mix-wakeup-irq = <0xfc00000 0x7d00 0x0 0x1400640>;  //唤醒源  Read supported wakeup source in M/F domain    gpc_mf_irqs[0，1，2，3]
			};

			iomuxc: iomuxc@020e0000 {                    //io复用控制器
				compatible = "fsl,imx6ul-iomuxc";
				reg = <0x020e0000 0x4000>;
			};

			gpr: iomuxc-gpr@020e4000 {                   //iomux-GPR
				compatible = "fsl,imx6ul-iomuxc-gpr",
					"fsl,imx6q-iomuxc-gpr", "syscon";
				reg = <0x020e4000 0x4000>;
			};

			mqs: mqs {                                   // Medium quality sound (MQS) 中等音质
				compatible = "fsl,imx6sx-mqs";
				gpr = <&gpr>;
				status = "disabled";
			};

			gpt2: gpt@020e8000 {                        //General Purpose Timer (GPT), 通用定时器2
				compatible = "fsl,imx6ul-gpt", "fsl,imx31-gpt";
				reg = <0x020e8000 0x4000>;
				interrupts = <GIC_SPI 109 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_DUMMY>,
					 <&clks IMX6UL_CLK_DUMMY>;
				clock-names = "ipg", "per";
			};

			sdma: sdma@020ec000 {                       //Smart Direct Memory Access Controller   Smart-DMA
				compatible = "fsl,imx6ul-sdma", "fsl,imx35-sdma";
				reg = <0x020ec000 0x4000>;
				interrupts = <GIC_SPI 2 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_SDMA>,
					 <&clks IMX6UL_CLK_SDMA>;
				clock-names = "ipg", "ahb";              //两个时钟配置ipg,ahb
				#dma-cells = <3>;                        //其它节点引用时传入三个参数 
				iram = <&ocram>;                        //On-Chip RAM Memory Controller (OCRAM) 片上RAM             
				fsl,sdma-ram-script-name = "imx/sdma/sdma-imx6q.bin";
			};

			pwm5: pwm@020f0000 {
				compatible = "fsl,imx6ul-pwm", "fsl,imx27-pwm";
				reg = <0x020f0000 0x4000>;
				interrupts = <GIC_SPI 114 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_DUMMY>,
					 <&clks IMX6UL_CLK_DUMMY>;
				clock-names = "ipg", "per";
				#pwm-cells = <2>;                      //#pwm-cells ，表示pwm5 可以被其它的节点引用，引用时，必须传入 2 个参数 如 <&pwm 1000000000 PWM_POLARITY_NORMAL>
			};

			pwm6: pwm@020f4000 {
				compatible = "fsl,imx6ul-pwm", "fsl,imx27-pwm";
				reg = <0x020f4000 0x4000>;
				interrupts = <GIC_SPI 115 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_DUMMY>,
					 <&clks IMX6UL_CLK_DUMMY>;
				clock-names = "ipg", "per";
				#pwm-cells = <2>;
			};

			pwm7: pwm@020f8000 {
				compatible = "fsl,imx6ul-pwm", "fsl,imx27-pwm";
				reg = <0x020f8000 0x4000>;
				interrupts = <GIC_SPI 116 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_DUMMY>,
					 <&clks IMX6UL_CLK_DUMMY>;
				clock-names = "ipg", "per";
				#pwm-cells = <2>;
			};

			pwm8: pwm@020fc000 {
				compatible = "fsl,imx6ul-pwm", "fsl,imx27-pwm";
				reg = <0x020fc000 0x4000>;
				interrupts = <GIC_SPI 117 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_DUMMY>,
					 <&clks IMX6UL_CLK_DUMMY>;
				clock-names = "ipg", "per";
				#pwm-cells = <2>;
			};
		};


    //AIPS-2
		aips2: aips-bus@02100000 { 
			compatible = "fsl,aips-bus", "simple-bus";
			#address-cells = <1>;
			#size-cells = <1>;
			reg = <0x02100000 0x100000>;
			ranges;                       //子节点地址空间 1:1 映射到 父地址空间
      //On-The-Go，即OTG技术就是实现在没有Host的情况下，实现设备间的数据传送。例如数码相机直接连接到打印机上，通过OTG技术，连接两台设备间的USB口，将拍出的相片立即打印出来;也可以将数码照相机中的数据，通过OTG发送到USB接口的移动硬盘上
			usbotg1: usb@02184000 {       
				compatible = "fsl,imx6ul-usb", "fsl,imx27-usb";
				reg = <0x02184000 0x200>;
				interrupts = <GIC_SPI 43 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_USBOH3>;
				fsl,usbphy = <&usbphy1>;   //usb phy1
				fsl,usbmisc = <&usbmisc 0>; //index-cell = 0
				fsl,anatop = <&anatop>;     //电源控制
				ahb-burst-config = <0x0>;
				tx-burst-size-dword = <0x10>;  //发送，突传大小 16 * 32位
				rx-burst-size-dword = <0x10>;
				status = "disabled";
			};

			usbotg2: usb@02184200 {
				compatible = "fsl,imx6ul-usb", "fsl,imx27-usb";
				reg = <0x02184200 0x200>;
				interrupts = <GIC_SPI 42 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_USBOH3>;
				fsl,usbphy = <&usbphy2>;
				fsl,usbmisc = <&usbmisc 1>; //index-cell =1
				ahb-burst-config = <0x0>;
				tx-burst-size-dword = <0x10>;
				rx-burst-size-dword = <0x10>;
				status = "disabled";
			};

			usbmisc: usbmisc@02184800 {  //USB 的部分 寄存器
				#index-cells = <1>;
				compatible = "fsl,imx6ul-usbmisc", "fsl,imx6q-usbmisc";
				reg = <0x02184800 0x200>;
			};

			fec1: ethernet@02188000 {   //网口1
				compatible = "fsl,imx6ul-fec", "fsl,imx6q-fec";
				reg = <0x02188000 0x4000>;
				interrupts = <GIC_SPI 118 IRQ_TYPE_LEVEL_HIGH>,
					     <GIC_SPI 119 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_ENET>,
					 <&clks IMX6UL_CLK_ENET_AHB>,
					 <&clks IMX6UL_CLK_ENET_PTP>,
					 <&clks IMX6UL_CLK_ENET_REF>,
					 <&clks IMX6UL_CLK_ENET_REF>;
				clock-names = "ipg", "ahb", "ptp",
					      "enet_clk_ref", "enet_out";
				stop-mode = <&gpr 0x10 3>;  
				fsl,num-tx-queues=<1>;  //发送队列个数
				fsl,num-rx-queues=<1>;
				fsl,magic-packet;       //远程唤醒支持
				fsl,wakeup_irq = <0>;
				status = "disabled";
                        };

			usdhc1: usdhc@02190000 {  //usb host controller ,usb 主控制器
				compatible = "fsl,imx6ull-usdhc", "fsl,imx6sx-usdhc";
				reg = <0x02190000 0x4000>;
				interrupts = <GIC_SPI 22 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_USDHC1>,
					 <&clks IMX6UL_CLK_USDHC1>,
					 <&clks IMX6UL_CLK_USDHC1>;
				clock-names = "ipg", "ahb", "per";
				assigned-clocks = <&clks IMX6UL_CLK_USDHC1_SEL>, <&clks IMX6UL_CLK_USDHC1>;
				assigned-clock-parents = <&clks IMX6UL_CLK_PLL2_PFD2>;
				assigned-clock-rates = <0>, <132000000>;           //时钟132Mhz
				bus-width = <4>;                                   //4 bytes
				fsl,tuning-step= <2>;                              //调整步长 2
				status = "disabled";
			};

			usdhc2: usdhc@02194000 {
				compatible = "fsl,imx6ull-usdhc", "fsl,imx6sx-usdhc";
				reg = <0x02194000 0x4000>;
				interrupts = <GIC_SPI 23 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_USDHC2>,
					 <&clks IMX6UL_CLK_USDHC2>,
					 <&clks IMX6UL_CLK_USDHC2>;
				clock-names = "ipg", "ahb", "per";
				assigned-clocks = <&clks IMX6UL_CLK_USDHC2_SEL>, <&clks IMX6UL_CLK_USDHC2>;
				assigned-clock-parents = <&clks IMX6UL_CLK_PLL2_PFD2>;
				assigned-clock-rates = <0>, <132000000>;
				bus-width = <4>;
				fsl,tuning-step= <2>;
				status = "disabled";
			};

			adc1: adc@02198000 {                                 //ADC
				compatible = "fsl,imx6ul-adc", "fsl,vf610-adc";
				reg = <0x02198000 0x4000>;
				interrupts = <GIC_SPI 100 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_ADC1>;
				num-channels = <2>;                                //ADC1-channel1 ADC1-channel2 
				clock-names = "adc";
				status = "disabled";
                        };

			i2c1: i2c@021a0000 {                                 //IIC
				#address-cells = <1>;
				#size-cells = <0>;
				compatible = "fsl,imx6ul-i2c", "fsl,imx21-i2c";
				reg = <0x021a0000 0x4000>;
				interrupts = <GIC_SPI 36 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_I2C1>;
				status = "disabled";
			};

			i2c2: i2c@021a4000 {                                 //IIC
				#address-cells = <1>;
				#size-cells = <0>;
				compatible = "fsl,imx6ul-i2c", "fsl,imx21-i2c";
				reg = <0x021a4000 0x4000>;
				interrupts = <GIC_SPI 37 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_I2C2>;
				status = "disabled";
			};

			i2c3: i2c@021a8000 {                                 //IIC
				#address-cells = <1>;
				#size-cells = <0>;
				compatible = "fsl,imx6ul-i2c", "fsl,imx21-i2c";
				reg = <0x021a8000 0x4000>;
				interrupts = <GIC_SPI 38 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_I2C3>;
				status = "disabled";
			};

			romcp@021ac000 {                                    //ROM Controller with Patch (ROMC) 带补丁的ROM控制器（ROMC）
				compatible = "fsl,imx6ul-romcp", "syscon";
				reg = <0x021ac000 0x4000>;
			};

			mmdc: mmdc@021b0000 {                              //Multi Mode DDR Controller (MMDC)   多模式DDR控制器（MMDC）
				compatible = "fsl,imx6ul-mmdc", "fsl,imx6q-mmdc";
				reg = <0x021b0000 0x4000>;
			};

			weim: weim@021b8000 {                              //W- External Interface Module (EIM) 外部总线接口
				compatible = "fsl,imx6ul-weim", "fsl,imx6q-weim";
				reg = <0x021b8000 0x4000>;
				interrupts = <GIC_SPI 14 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_DUMMY>;
			};

			ocotp: ocotp-ctrl@021bc000 {                       //On-Chip OTP Controller (OCOTP_CTRL) 一次性可编程存储器 === eFUSE   有点像 stm32 中的 option bytes
				                                                 //Efuse类似于EEPROM，是一次性可编程存储器，在芯片出场之前会被写入信息，在一个芯片中，efuse的容量通常很小，一些芯片efuse只有128bit。
				compatible = "fsl,imx6ull-ocotp", "syscon";      //syscon,应该是系统控制字节
				reg = <0x021bc000 0x4000>;
				clocks = <&clks IMX6UL_CLK_OCOTP>;
			};

			csu: csu@021c0000 {                              //Central Security Unit  中央安全单元     在 Chapter 11 System Security
				compatible = "fsl,imx6ul-csu";
				reg = <0x021c0000 0x4000>;
				interrupts = <GIC_SPI 21 IRQ_TYPE_LEVEL_HIGH>;
				status = "disabled";
			};

			csi: csi@021c4000 {                             //CMOS Sensor Interface (CSI) CMOS传感器接口（CSI）
				compatible = "fsl,imx6ul-csi", "fsl,imx6s-csi";
				reg = <0x021c4000 0x4000>;
				interrupts = <GIC_SPI 7 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_DUMMY>,
					<&clks IMX6UL_CLK_CSI>,
					<&clks IMX6UL_CLK_DUMMY>;
				clock-names = "disp-axi", "csi_mclk", "disp_dcic";
				status = "disabled";
			};

			lcdif: lcdif@021c8000 {                        //Enhanced LCD Interface (eLCDIF) LCD接口
				compatible = "fsl,imx6ul-lcdif", "fsl,imx28-lcdif";
				reg = <0x021c8000 0x4000>;
				interrupts = <GIC_SPI 5 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_LCDIF_PIX>,
					 <&clks IMX6UL_CLK_LCDIF_APB>,
					 <&clks IMX6UL_CLK_DUMMY>;
				clock-names = "pix", "axi", "disp_axi";
				status = "disabled";
			};

			pxp: pxp@021cc000 {                          //Pixel Pipeline (PXP) 像素处理管道     发送到LCD之前  先处理图形缓冲区或复合视频和图形数据显示器或电视编码器。
				compatible = "fsl,imx6ull-pxp-dma", "fsl,imx7d-pxp-dma";
				reg = <0x021cc000 0x4000>;
				interrupts = <GIC_SPI 8 IRQ_TYPE_LEVEL_HIGH>,
					<GIC_SPI 18 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_DUMMY>, <&clks IMX6UL_CLK_PXP>;
				clock-names = "pxp_ipg", "pxp_axi";
				status = "disabled";
			};
//1. 标准SPI          标准SPI通常就称SPI，它是一种串行外设接口规范，有4根引脚信号：clk , cs, mosi, miso
//2. Dual SPI         它只是针对SPI Flash而言，不是针对所有SPI外设。对于SPI Flash，全双工并不常用，因此扩展了mosi和miso的用法，让它们工作在半双工，用以加倍数据传输。也就是对于Dual SPI Flash，可以发送一个命令字节进入dual mode，这样mosi变成SIO0（serial io 0），mosi变成SIO1（serial io 1）,这样一个时钟周期内就能传输2个bit数据，加倍了数据传输
//3. Qual SPI         与Dual SPI类似，也是针对SPI Flash，Qual SPI Flash增加了两根I/O线（SIO2,SIO3），目的是一个时钟内传输4个bit
			qspi: qspi@021e0000 {                       //Quad Serial Peripheral Interface (QuadSPI) 四路串行外设接口（QuadSPI）,一次传4bit
				#address-cells = <1>;
				#size-cells = <0>;
				compatible = "fsl,imx6ull-qspi", "fsl,imx6ul-qspi";
				reg = <0x021e0000 0x4000>, <0x60000000 0x10000000>;
				reg-names = "QuadSPI", "QuadSPI-memory";
				interrupts = <GIC_SPI 107 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_QSPI>,
					 <&clks IMX6UL_CLK_QSPI>;
				clock-names = "qspi_en", "qspi";
				status = "disabled";
			};

			uart2: serial@021e8000 {                  //UART
				compatible = "fsl,imx6ul-uart",
					     "fsl,imx6q-uart", "fsl,imx21-uart";
				reg = <0x021e8000 0x4000>;
				interrupts = <GIC_SPI 27 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_UART2_IPG>,
					 <&clks IMX6UL_CLK_UART2_SERIAL>;
				clock-names = "ipg", "per";
				dmas = <&sdma 27 4 0>, <&sdma 28 4 0>; //uast引用DMA   <dma通道 4bytes?  ?>
				dma-names = "rx", "tx";
				status = "disabled";
			};

			uart3: serial@021ec000 {                  //UART
				compatible = "fsl,imx6ul-uart",
					     "fsl,imx6q-uart", "fsl,imx21-uart";
				reg = <0x021ec000 0x4000>;
				interrupts = <GIC_SPI 28 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_UART3_IPG>,
					 <&clks IMX6UL_CLK_UART3_SERIAL>;
				clock-names = "ipg", "per";
				dmas = <&sdma 29 4 0>, <&sdma 30 4 0>;
				dma-names = "rx", "tx";
				status = "disabled";
			};

			uart4: serial@021f0000 {                  //UART
				compatible = "fsl,imx6ul-uart",
					     "fsl,imx6q-uart", "fsl,imx21-uart";
				reg = <0x021f0000 0x4000>;
				interrupts = <GIC_SPI 29 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_UART4_IPG>,
					 <&clks IMX6UL_CLK_UART4_SERIAL>;
				clock-names = "ipg", "per";
				dmas = <&sdma 31 4 0>, <&sdma 32 4 0>;
				dma-names = "rx", "tx";
				status = "disabled";
			};

			uart5: serial@021f4000 {                  //UART
				compatible = "fsl,imx6ul-uart",
					     "fsl,imx6q-uart", "fsl,imx21-uart";
				reg = <0x021f4000 0x4000>;
				interrupts = <GIC_SPI 30 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_UART5_IPG>,
					 <&clks IMX6UL_CLK_UART5_SERIAL>;
				clock-names = "ipg", "per";
				dmas = <&sdma 33 4 0>, <&sdma 34 4 0>;
				dma-names = "rx", "tx";
				status = "disabled";
			};

			i2c4: i2c@021f8000 {                                 //IIC
				#address-cells = <1>;
				#size-cells = <0>;
				compatible = "fsl,imx6ul-i2c", "fsl,imx21-i2c";
				reg = <0x021f8000 0x4000>;
				interrupts = <GIC_SPI 35 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_I2C4>;
				status = "disabled";
			};

			uart6: serial@021fc000 {                  //UART
				compatible = "fsl,imx6ul-uart",
					     "fsl,imx6q-uart", "fsl,imx21-uart";
				reg = <0x021fc000 0x4000>;
				interrupts = <GIC_SPI 17 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_UART6_IPG>,
					 <&clks IMX6UL_CLK_UART6_SERIAL>;
				clock-names = "ipg", "per";
				dmas = <&sdma 0 4 0>, <&sdma 47 4 0>;
				dma-names = "rx", "tx";
				status = "disabled";
			};
		};
    //AIPS-3
		aips3: aips-bus@02200000 {                       //
			compatible = "fsl,aips-bus", "simple-bus";
			#address-cells = <1>;
			#size-cells = <1>;
			reg = <0x02200000 0x100000>;
			ranges;
//为了安全起见，数据协处理器（DCP）为
//密码算法。 DCP的功能有：
//•加密算法：AES-128（ECB和CBC模式）
//•哈希算法：SHA-1和SHA-256
//•从SNVS，DCP内部密钥存储或常规存储器中选择密钥
//•内部存储器，最多可存储四个AES-128密钥-将密钥写入到
//钥匙槽，只能由DCP AES-128引擎读取
//•IP从接口
//•DMA
			dcp: dcp@02280000 {                           //Data Co-Processor (DCP)  数据协处理器（DCP） 11.4 Data Co-Processor (DCP)
				compatible = "fsl,imx6sl-dcp";
				reg = <0x02280000 0x4000>;
				interrupts = <GIC_SPI 46 IRQ_TYPE_LEVEL_HIGH>,
					     <GIC_SPI 47 IRQ_TYPE_LEVEL_HIGH>,
					     <GIC_SPI 48 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_DCP_CLK>;
				clock-names = "dcp";
			};

			rngb: rngb@02284000 {                          //Random Number Generator (RNGB)随机数发生器
				compatible = "fsl,imx6sl-rng", "fsl,imx-rng", "imx-rng";
				reg = <0x02284000 0x4000>;
				interrupts = <GIC_SPI 6 IRQ_TYPE_LEVEL_HIGH>;
				clocks =  <&clks IMX6UL_CLK_DUMMY>;
			};

			uart8: serial@02288000 {                   //UART
				compatible = "fsl,imx6ul-uart",
					     "fsl,imx6q-uart", "fsl,imx21-uart";
				reg = <0x02288000 0x4000>;
				interrupts = <GIC_SPI 40 IRQ_TYPE_LEVEL_HIGH>;
				clocks = <&clks IMX6UL_CLK_UART8_IPG>,
					 <&clks IMX6UL_CLK_UART8_SERIAL>;
				clock-names = "ipg", "per";
				dmas = <&sdma 45 4 0>, <&sdma 46 4 0>;
				dma-names = "rx", "tx";
				status = "disabled";
			};

			epdc: epdc@0228c000 {                     //Electrophoretic Display Controller (EPDC)  电泳显示控制器（EPDC）主要用于驱动LCD的刷新： TFT分辨率高达4096 x 4096像素，并具有20 Hz刷新（可编程至8191 x 8191）
				                                        //刷新106 Hz时TFT分辨率高达1650 x 2332像素。。。。。。。
				compatible = "fsl,imx7d-epdc";
				interrupts = <GIC_SPI 112 IRQ_TYPE_LEVEL_HIGH>;
				reg = <0x0228c000 0x4000>;
				clocks = <&clks IMX6UL_CLK_EPDC_ACLK>,
					 <&clks IMX6UL_CLK_EPDC_PIX>;
				clock-names = "epdc_axi", "epdc_pix";
				/* Need to fix epdc-ram */
				/* epdc-ram = <&gpr 0x4 30>; */
				status = "disabled";
			};

			iomuxc_snvs: iomuxc-snvs@02290000 {       //snvs 的io复用
				compatible = "fsl,imx6ull-iomuxc-snvs";
				reg = <0x02290000 0x10000>;
			};

			snvs_gpr: snvs-gpr@0x02294000 {           //SNVS 的GPR
				compatible = "fsl, imx6ull-snvs-gpr";
				reg = <0x02294000 0x10000>;
			};
		};
	};
};
