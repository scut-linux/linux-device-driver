
//: ====================================��ʽ˵��=================================================================================//
//: --------------------����---------------------------------------//
compatible = manufacturer,model
//����:����ϵͳ������
//manufacturer: �����̣���Ʒ��     ��:  acme,coyotes-revenge"
//model       : ��׼�����豸     
//����:   compatible = "arm,vexpress-flash", "cfi-flash";  


reg = <address1 [length1] [address2 [length2]] [address3 [length3]] ... >     // �Ĵ���=<��ַ [����]   [��ַ [����]]...>  ���ȿ�ѡ,��#size-cells = <0> ʱ�������򲻴���
//����:����Ĵ���
//address1,...  :�Ĵ�����ַ
//length1,...   :��ַ����,��ѡ
//����:   #address-cells = <1>; ʱ  reg = <0x101f2000 0x1000 >;         //��ַ=0x101f2000              ��ַ����=0x1000               
//        #address-cells = <2>; ʱ  reg = <2 0 0x4000000>;              //��ַ=2 0  Ƭѡ2   ��ַƫ��0  ��ַ����=0x4000000          


#address-cells = <n>
//����:�����ַ�����
//n   :��ַ�����
//����:   #address-cells = <1>  address ʹ�� 1���޷���32λ��ʾ
//        #address-cells = <2>  address ʹ�� 2���޷���32λ��ʾ, �� Ƭѡ+ƫ��
 
#size-cells = <n>
//����:�����ַ����
//n   :��ַ���ȸ���0,1,2...  

<name>[@<unit-address>]
//����:����ڵ���
//name:�ڵ���,�豸��
//unit-address:�豸�׵�ַ //����ϰ�ߣ�����ڵ����reg���ԣ���ô�ڵ������������Ԫ��ַ��������reg�����еĵ�һ����ַ��ֵ
//����:   serial@101f0000{}   
//flash@2,0 {                                                 //��ַ��Ϊ:2,0
//            compatible = "samsung,k8f1315ebm", "cfi-flash";
//            reg = <2 0 0x4000000>;                          //���ڵ�ַ����==2����ַ��==2 0 ,���Խڵ���Ϊ flash@2,0
//        };    
// ranges;  �ա�ranges�����ԵĴ�����ζ���ӵ�ַ�ռ��еĵ�ַ��1:1ӳ�䵽����ַ�ռ䡣

ranges = <address  cpuaddress ....>
//����:�豸��ַת��Ϊcpu��ַ
// address  :�豸��ַ���豸��ַֻ���豸����Ч�����ܱ�cpu����
//cpuaddress:cpu��ַ
//����:	   ranges = <0 0 0x10100000 0x10000 //Chipselect 1 -> ethernet    //#address-cells = <2>
//	   	             1 0 0x10160000 0x10000 //Chipselect 2 -> i2c
//	   	             2 0 0x30000000 0x1000000>;// Chipselect 3 -> flash

//: --------------------�ж�---------------------------------------//
 intc:interrupt-controller@10140000{
 		compatible = "arm,pl190";
 		reg = <0x10140000 0x1000>;
 			
 		interrupt-controller;           /*�յ�����:�ýڵ�����ж���Ϣ*/
 		#interrupt-cells = <2>;         /*�ж�����źŸ���:interrupts = <�ж�����ź� ��Ǻ�>*/
 };
 
interrupt-parent = <&intc>;         //�����������ڣ����нڵ��Ĭ�ϸ��ڵ���intc,Ҳ����Ĭ��
//����: ����ڵ����õĸ��ڵ�


serial@101F0000{
	compatible = "arm,pl011";
	reg = <0x101f0000 0x1000>;
	interrupts = <1 0>;             /*<�жϺ� [����������] [���ȼ�]>*/ 
	                                /*interrupts = <interrupt type interrupt number trigger type> spi�ж�����:�ж�����,�жϺ�,��������*/
};
//interrupts: �����жϺţ�ʹ�� 2��ֵ����
		
//intc:interrupt-controller@10140000 :����: �����жϿ������ڵ�
//intc: �жϽڵ�����,�� gic: interrupt-controller@10490000{...}
//interrupt-controller  :��ʾintc�ڵ��ǽ����жϵĽڵ�
//interrupt-cells       :��ʾ�����жϺŵĸ�����serial@101F0000 ���ж���intc, ��intc ��interrupt-cells==2,���� serial �����ж�:interrupts =<xx xx> ʹ����2��ֵ����
//interrupt-names       :�ж������� platform_get_irq_byname(pdev,"name") �е� name  һ�´�


interrupt-map-mask = <>
interrupt-map = <>







/*dependants*/

/*root*/
/ {
		compatible = "acme,coyotes-revenge"; /*���ݵģ����̣�������*/ /*compatible = "�豸��Ϣ,���ݵ��豸"*/
	
	  /*Ѱַ:ȫ��*/
	  #address-cells = <1>;                /*��ַ��:1��32λ*/
	  #size-cells = <o>;                   /*������:0��32λ*/
	  	
	  /*�жϸ��ڵ�*/
	  interrupt-parent = <&intc>;
	
	
		/*CPU��Ϣ:˫��A9*/
		cpus{
			/*Ѱַ:�ֲ�*/
			#address-cells = <1>;
			#size-cells = <0>;
				
			cpu@0{
				compatible = "arm,cortex-a9"; /*������,�ͺ�*/
				reg = <0>;                    /*Ѱַ*/
			};
			
			cpu@1{
				compatible = "arm,cortex-a9"; /*������,�ͺ�*/
				reg = <1>;                    /*Ѱַ*/
			};
		};
		
		/*�ڵ� <�豸����>[@<�豸����ַ>]*/
			
		/*���������豸*/
		serial@101F0000{
			compatible = "arm,pl011";
			reg = <0x101f0000 0x1000>;
			interrupts = <1 0>;             /*�ж�����ź�,��Ǻ�*/
		};
		serial@101F2000{
			compatible = "arm,pl011";
			reg = <0x101f2000 0x1000>;
			interrupts = <2 0>;             /*�ж�����ź�,��Ǻ�*/
		};
		
		/*GPIO*/
		gpio@101F3000{
			compatible = "arm,pl061";
			reg = <0x101f3000 0x1000 0x101f4000 0x0010>;
			interrupts = <3 0>;             /*�ж�����ź�,��Ǻ�*/
		};
	
	 /*�жϿ�����*/
	 intc:interrupt-controller@10140000{
	 		compatible = "arm,pl190";
	 		reg = <0x10140000 0x1000>;
	 			
	 		interrupt-controller;           /*�յ�����:�ýڵ�����ж���Ϣ*/
	 		#interrupt-cells = <2>;         /*�ж�����źŸ���:interrupts = <�ж�����ź� ��Ǻ�>*/
	 };
	 
	 /*spi*/
	 spi@10115000{
	 		compatible = "arm,pl022";
	 		reg = <0x10115000 0x1000>;
	 		interrupts = <4 0 >;
	 };
	 
	 /*�ⲿ�����豸*/
	 external-bus{
	 
	   /*��ַӳ��*/
	   ranges = <0 0 0x10100000 0x10000 //Chipselect 1 -> ethernet
	   	         1 0 0x10160000 0x10000 //Chipselect 2 -> i2c
	   	         2 0 0x30000000 0x1000000>;// Chipselect 3 -> flash
	 
	 
	 
	    /*Ѱַ: �ֲ�,����ź�*/
	    #address-cells = <2>;       // ʹ�� 2 �� u32 ����� address����i2c@1,0  flash@2,0
	    #size-cells = <1>;          // ʹ�� 1 �� u32 ����� size
	 
	 		/*���� Ƭѡ��0,ƫ����0*/
	 		ethernet@0,0{
	 			compatible = "smc,smc91c111";
	 			reg = <0 0 0x1000>;      //��ַ��: 0 0   ��ַ����0x1000
	 			interrupts = <5 2>;      /*�ж�����ź�,��Ǻ�*/
	 		};
	 		
	 		/*ic2 Ƭѡ��1,ƫ����0*/
	 		i2c@1,0{                  //�ڵ���i2c  �׵�ַ:1,0
	 			compatible = "acme,a1234-i2c-bus";
	 			reg = <1 0 0x1000>;      //��ַ��:1 0    ��ַ����0x1000
	 			interrupts = <6 2>;	    /*�ж�����ź�,��Ǻ�*/
	 			rtc@58{
	 				compatible = "maxim,ds1338";
	 				reg = <58>;
          interrupts = <7 3>;   /*�ж�����ź�,��Ǻ�*/
	 			};
	 		};
	 		
	 		/*flash Ƭѡ��2,ƫ����0*/
	 		flash@2,0{
	 			compatible = "samsung,k8f1315ebm","cfi-flash";
	 			reg = <2 0 0x4000000>;
	 		};
	 };
	 
	 
	 /*PCI����*/
	 PCI@0x10180000{
	 		compatible = "arm,versatile-pci-hostbridge","pci";
	 		reg = <0x10180000 0x1000>;
	 		interrupts = <8 0>;     /*�ж�����ź�,��Ǻ�*/
	 		bus-ranges = <0 0>;
	 		
	 		/*��ַת��*/
	 		#address-cells = <3>;
	 		#size-cells = <2>;
	 		        /*  phys.hi phys.mid phys.low */
	 		ranges =<0x42000000 0        0x80000000 0x80000000 0 0x20000000 //(0x80000000->0x80000000)// PCI��ַΪ0x80000000 ��СΪ512MԤ���ַ����ӳ�䵽��CPU�ڴ��ַΪ0x80000000 ����
							 0x02000000 0        0xa0000000 0xa0000000 0 0x10000000 //(0xa0000000->0xa0000000)// PCI��ַΪ0xa0000000��СΪ512M��Ԥ���ַ����ӳ�䵽��CPU�ڴ��ַΪ0xa0000000��
               0x01000000 0        0x00000000 0xb0000000 0 0x01000000 //(0x00000000->0xb0000000)// PCI��ַΪ0x00000000��СΪ16M IO����ӳ�䵽��CPU�ڴ��ַΪ0xb0000000����
               >;
               
       /*�߼��ж�ӳ��*/
       #interrupt-cells = <1>; /*�ж�����źŸ���:interrupts = <�ж�����ź�>*/
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



/*�Զ�������*/



/*����*/