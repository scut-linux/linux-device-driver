
//: ====================================格式说明=================================================================================//
//: --------------------常用---------------------------------------//
compatible = manufacturer,model
//作用:定义系统的名称
//manufacturer: ”厂商，产品“     如:  acme,coyotes-revenge"
//model       : 标准类型设备     
//例如:   compatible = "arm,vexpress-flash", "cfi-flash";  


reg = <address1 [length1] [address2 [length2]] [address3 [length3]] ... >     // 寄存器=<地址 [长度]   [地址 [长度]]...>  长度可选,当#size-cells = <0> 时，长度域不存在
//作用:定义寄存器
//address1,...  :寄存器地址
//length1,...   :地址长度,可选
//例如:   #address-cells = <1>; 时  reg = <0x101f2000 0x1000 >;         //地址=0x101f2000              地址长度=0x1000               
//        #address-cells = <2>; 时  reg = <2 0 0x4000000>;              //地址=2 0  片选2   地址偏移0  地址长度=0x4000000          


#address-cells = <n>
//作用:定义地址域个数
//n   :地址域个数
//例如:   #address-cells = <1>  address 使用 1个无符号32位表示
//        #address-cells = <2>  address 使用 2个无符号32位表示, 如 片选+偏移
 
#size-cells = <n>
//作用:定义地址长度
//n   :地址长度个数0,1,2...  

<name>[@<unit-address>]
//作用:定义节点名
//name:节点名,设备名
//unit-address:设备首地址 //按照习惯，如果节点具有reg属性，那么节点名必须包含单元地址，而且是reg属性中的第一个地址的值
//例如:   serial@101f0000{}   
//flash@2,0 {                                                 //地址域为:2,0
//            compatible = "samsung,k8f1315ebm", "cfi-flash";
//            reg = <2 0 0x4000000>;                          //由于地址长度==2，地址域==2 0 ,所以节点名为 flash@2,0
//        };    
// ranges;  空“ranges”属性的存在意味着子地址空间中的地址将1:1映射到父地址空间。

ranges = <address  cpuaddress ....>
//作用:设备地址转换为cpu地址
// address  :设备地址，设备地址只在设备域有效，不能被cpu访问
//cpuaddress:cpu地址
//例如:	   ranges = <0 0 0x10100000 0x10000 //Chipselect 1 -> ethernet    //#address-cells = <2>
//	   	             1 0 0x10160000 0x10000 //Chipselect 2 -> i2c
//	   	             2 0 0x30000000 0x1000000>;// Chipselect 3 -> flash

//: --------------------中断---------------------------------------//
 intc:interrupt-controller@10140000{
 		compatible = "arm,pl190";
 		reg = <0x10140000 0x1000>;
 			
 		interrupt-controller;           /*空的属性:该节点接受中断信息*/
 		#interrupt-cells = <2>;         /*中断输出信号个数:interrupts = <中断输出信号 标记号>*/
 };
 
interrupt-parent = <&intc>;         //所在作用域内，所有节点的默认父节点是intc,也就是默认
//作用: 定义节点引用的父节点


serial@101F0000{
	compatible = "arm,pl011";
	reg = <0x101f0000 0x1000>;
	interrupts = <1 0>;             /*<中断号 [触发的类型] [优先级]>*/ 
	                                /*interrupts = <interrupt type interrupt number trigger type> spi中断描述:中断类型,中断号,触发类型*/
};
//interrupts: 描述中断号，使用 2个值描述
		
//intc:interrupt-controller@10140000 :作用: 定义中断控制器节点
//intc: 中断节点名称,如 gic: interrupt-controller@10490000{...}
//interrupt-controller  :表示intc节点是接受中断的节点
//interrupt-cells       :表示描述中断号的个数，serial@101F0000 父中断是intc, 而intc 的interrupt-cells==2,所以 serial 描述中断:interrupts =<xx xx> 使用用2个值描述
//interrupt-names       :中断名，与 platform_get_irq_byname(pdev,"name") 中的 name  一致辞


interrupt-map-mask = <>
interrupt-map = <>







/*dependants*/

/*root*/
/ {
		compatible = "acme,coyotes-revenge"; /*兼容的，厂商，制造商*/ /*compatible = "设备信息,兼容的设备"*/
	
	  /*寻址:全局*/
	  #address-cells = <1>;                /*地址域:1个32位*/
	  #size-cells = <o>;                   /*长度域:0个32位*/
	  	
	  /*中断父节点*/
	  interrupt-parent = <&intc>;
	
	
		/*CPU信息:双核A9*/
		cpus{
			/*寻址:局部*/
			#address-cells = <1>;
			#size-cells = <0>;
				
			cpu@0{
				compatible = "arm,cortex-a9"; /*制造商,型号*/
				reg = <0>;                    /*寻址*/
			};
			
			cpu@1{
				compatible = "arm,cortex-a9"; /*制造商,型号*/
				reg = <1>;                    /*寻址*/
			};
		};
		
		/*节点 <设备类型>[@<设备基地址>]*/
			
		/*两个串口设备*/
		serial@101F0000{
			compatible = "arm,pl011";
			reg = <0x101f0000 0x1000>;
			interrupts = <1 0>;             /*中断输出信号,标记号*/
		};
		serial@101F2000{
			compatible = "arm,pl011";
			reg = <0x101f2000 0x1000>;
			interrupts = <2 0>;             /*中断输出信号,标记号*/
		};
		
		/*GPIO*/
		gpio@101F3000{
			compatible = "arm,pl061";
			reg = <0x101f3000 0x1000 0x101f4000 0x0010>;
			interrupts = <3 0>;             /*中断输出信号,标记号*/
		};
	
	 /*中断控制器*/
	 intc:interrupt-controller@10140000{
	 		compatible = "arm,pl190";
	 		reg = <0x10140000 0x1000>;
	 			
	 		interrupt-controller;           /*空的属性:该节点接受中断信息*/
	 		#interrupt-cells = <2>;         /*中断输出信号个数:interrupts = <中断输出信号 标记号>*/
	 };
	 
	 /*spi*/
	 spi@10115000{
	 		compatible = "arm,pl022";
	 		reg = <0x10115000 0x1000>;
	 		interrupts = <4 0 >;
	 };
	 
	 /*外部总线设备*/
	 external-bus{
	 
	   /*地址映射*/
	   ranges = <0 0 0x10100000 0x10000 //Chipselect 1 -> ethernet
	   	         1 0 0x10160000 0x10000 //Chipselect 2 -> i2c
	   	         2 0 0x30000000 0x1000000>;// Chipselect 3 -> flash
	 
	 
	 
	    /*寻址: 局部,多个信号*/
	    #address-cells = <2>;       // 使用 2 個 u32 來代表 address。如i2c@1,0  flash@2,0
	    #size-cells = <1>;          // 使用 1 個 u32 來代表 size
	 
	 		/*网络 片选号0,偏移量0*/
	 		ethernet@0,0{
	 			compatible = "smc,smc91c111";
	 			reg = <0 0 0x1000>;      //地址域: 0 0   地址长度0x1000
	 			interrupts = <5 2>;      /*中断输出信号,标记号*/
	 		};
	 		
	 		/*ic2 片选号1,偏移量0*/
	 		i2c@1,0{                  //节点名i2c  首地址:1,0
	 			compatible = "acme,a1234-i2c-bus";
	 			reg = <1 0 0x1000>;      //地址域:1 0    地址长度0x1000
	 			interrupts = <6 2>;	    /*中断输出信号,标记号*/
	 			rtc@58{
	 				compatible = "maxim,ds1338";
	 				reg = <58>;
          interrupts = <7 3>;   /*中断输出信号,标记号*/
	 			};
	 		};
	 		
	 		/*flash 片选号2,偏移量0*/
	 		flash@2,0{
	 			compatible = "samsung,k8f1315ebm","cfi-flash";
	 			reg = <2 0 0x4000000>;
	 		};
	 };
	 
	 
	 /*PCI总线*/
	 PCI@0x10180000{
	 		compatible = "arm,versatile-pci-hostbridge","pci";
	 		reg = <0x10180000 0x1000>;
	 		interrupts = <8 0>;     /*中断输出信号,标记号*/
	 		bus-ranges = <0 0>;
	 		
	 		/*地址转换*/
	 		#address-cells = <3>;
	 		#size-cells = <2>;
	 		        /*  phys.hi phys.mid phys.low */
	 		ranges =<0x42000000 0        0x80000000 0x80000000 0 0x20000000 //(0x80000000->0x80000000)// PCI地址为0x80000000 大小为512M预存地址区，映射到主CPU内存地址为0x80000000 处。
							 0x02000000 0        0xa0000000 0xa0000000 0 0x10000000 //(0xa0000000->0xa0000000)// PCI地址为0xa0000000大小为512M无预存地址区，映射到主CPU内存地址为0xa0000000处
               0x01000000 0        0x00000000 0xb0000000 0 0x01000000 //(0x00000000->0xb0000000)// PCI地址为0x00000000大小为16M IO区，映射到主CPU内存地址为0xb0000000处。
               >;
               
       /*高级中断映射*/
       #interrupt-cells = <1>; /*中断输出信号个数:interrupts = <中断输出信号>*/
       interrupt-map-mask = <0xf800 0 0 7>;
       interrupt-map = < 0xc000 0 0 1 &intc 9 3   //1st slot
                         0xc000 0 0 2 &intc 10 3
                         0xc000 0 0 3 &intc 11 3
                         0xc000 0 0 4 &intc 12 3
                         
                         0xc800 0 0 1 &intc 10 3  //2nd slot
                         0xc800 0 0 2 &intc 11 3
                         0xc800 0 0 3 &intc 12 3
                         0xc800 0 0 4 &intc 9 3>;
	 };
};



/*自定义数据*/



/*别名*/