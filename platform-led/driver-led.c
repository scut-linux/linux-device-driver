#include <linux/types.h>
#include <linux/kernel.h>
#include <linux/delay.h>
#include <linux/ide.h>
#include <linux/init.h>
#include <linux/module.h>
#include <linux/errno.h>
#include <linux/gpio.h>
#include <linux/cdev.h>
#include <linux/device.h>
#include <linux/of_gpio.h>
#include <linux/semaphore.h>
#include <linux/timer.h>
#include <linux/irq.h>
#include <linux/wait.h>
#include <linux/poll.h>
#include <linux/fs.h>
#include <linux/fcntl.h>
#include <linux/platform_device.h>

#include <asm/mach/map.h>
#include <asm/uaccess.h>
#include <asm/io.h>

/*===============================1.设备驱动====================================*/

/*===============================================================================
 * GPIO1_IO03作为led
 * 时钟 IPG_CLK, 时钟控制　CCGR1
 * Pad 对应的是Soc芯片上的引脚
 * Mux 利用控制器
 * ===============================================================================
 * */
#define LEDDEV_CNT 1            /*设备号长度*/
#define LEDDEV_NAME "platled"   /*设备名字*/
#define LEDOFF 	0				/* 关灯 */
#define LEDON 	1				/* 开灯 */

//虚拟内存地址
static void __iomem *IMX6U_CCM_CCGR1;
static void __iomem *SW_MUX_GPIO1_IO03;
static void __iomem *SW_PAD_GPIO1_IO03;
static void __iomem *GPIO1_DR;
static void __iomem *GPIO1_GDIR;


//leddev 设备结构体
struct leddev_dev{
	dev_t devid; //设备号
	struct cdev cdev; //cdev
	struct class *class; //类
	struct device* device;//设备
	int major;     //主设备号	
};
struct leddev_dev leddev;  //led 设备实例


//初始化IO
static void _mygpio_init(struct resource *src[],int ressize[])
{
    u32 val = 0;

    /* 初始化LED */
    /* 1、寄存器地址映射 ===>使用资源*/
    IMX6U_CCM_CCGR1 = ioremap(src[0]->start, ressize[0]);
    SW_MUX_GPIO1_IO03 = ioremap(src[1]->start, ressize[1]);
    SW_PAD_GPIO1_IO03 = ioremap(src[2]->start, ressize[2]);
    GPIO1_DR = ioremap(src[3]->start, ressize[3]);
    GPIO1_GDIR = ioremap(src[4]->start, ressize[4]);

    /* 2、使能GPIO1时钟
     * 27–26 CG13  gpio1 clock (gpio1_clk_enable)
     * */
    val = readl(IMX6U_CCM_CCGR1);
    val &= ~(3 << 26);	/* 清楚以前的设置 */
    val |= (3 << 26);	/* 设置新值 */
    writel(val, IMX6U_CCM_CCGR1);

    /* 3、设置GPIO1_IO03的复用功能，将其复用为
     *    GPIO1_IO03，最后设置IO属性。
     *    0101 ALT5 — Select mux mode: ALT5 mux port: GPIO1_IO03 of instance: gpio1
     */
    writel(5, SW_MUX_GPIO1_IO03);

    /*寄存器SW_PAD_GPIO1_IO03设置IO属性
     *bit 16:0 HYS关闭
     *bit [15:14]: 00 默认下拉
     *bit [13]: 0 kepper功能
     *bit [12]: 1 pull/keeper使能
     *bit [11]: 0 关闭开路输出
     *bit [7:6]: 10 速度100Mhz
     *bit [5:3]: 110 R0/6驱动能力
     *bit [0]: 0 低转换率
     */
    writel(0x10B0, SW_PAD_GPIO1_IO03);

    /* 4、设置GPIO1_IO03为输出功能  1 OUTPUT — GPIO is configured as output. */
    val = readl(GPIO1_GDIR);
    val &= ~(1 << 3);	/* 清除以前的设置 */
    val |= (1 << 3);	/* 设置为输出 */
    writel(val, GPIO1_GDIR);

    /* 5、默认关闭LED */
    val = readl(GPIO1_DR);
    val |= (1 << 3);
    writel(val, GPIO1_DR);
}

//io设置值
void _myled_switch(u8 sta)
{
    u32 val = 0;
    if(sta == LEDON) {
        val = readl(GPIO1_DR);
        val &= ~(1 << 3);
        writel(val, GPIO1_DR);
    }else if(sta == LEDOFF) {
        val = readl(GPIO1_DR);
        val|= (1 << 3);
        writel(val, GPIO1_DR);
    }
}

//取消gpio映射
void _myled_unmap(void)
{
    /* 取消映射 */
    iounmap(IMX6U_CCM_CCGR1);
    iounmap(SW_MUX_GPIO1_IO03);
    iounmap(SW_PAD_GPIO1_IO03);
    iounmap(GPIO1_DR);
    iounmap(GPIO1_GDIR);
}

/*===============================================================================
    字符设备接口
 * =============================================================================*/
/*
 * @description		: 打开设备
 * @param - inode 	: 传递给驱动的inode
 * @param - filp 	: 设备文件，file结构体有个叫做private_data的成员变量
 * 					  一般在open的时候将private_data指向设备结构体。
 * @return 			: 0 成功;其他 失败
 */
static int led_open(struct inode *inode, struct file *filp)
{
    return 0;
}
//??
/*
 * @description		: 从设备读取数据
 * @param - filp 	: 要打开的设备文件(文件描述符)
 * @param - buf 	: 返回给用户空间的数据缓冲区
 * @param - cnt 	: 要读取的数据长度
 * @param - offt 	: 相对于文件首地址的偏移
 * @return 			: 读取的字节数，如果为负值，表示读取失败
 */
static ssize_t led_read(struct file *filp, char __user *buf, size_t cnt, loff_t *offt)
{
    return 0;
}

/*
 * @description		: 向设备写数据
 * @param - filp 	: 设备文件，表示打开的文件描述符
 * @param - buf 	: 要写给设备写入的数据
 * @param - cnt 	: 要写入的数据长度
 * @param - offt 	: 相对于文件首地址的偏移
 * @return 			: 写入的字节数，如果为负值，表示写入失败
 */
static ssize_t led_write(struct file *filp, const char __user *buf, size_t cnt, loff_t *offt)
{
    int retvalue;
    unsigned char databuf[1];
    unsigned char ledstat;

    retvalue = copy_from_user(databuf, buf, cnt);//数据不能直接从用户空间传到内核空间，必须使用copy_from_user
    if(retvalue < 0) {
        printk("kernel write failed!\r\n");
        return -EFAULT;
    }

    ledstat = databuf[0];		/* 获取状态值 */

    if(ledstat == LEDON) {
        _myled_switch(LEDON);		/* 打开LED灯 */
    } else if(ledstat == LEDOFF) {
        _myled_switch(LEDOFF);	/* 关闭LED灯 */
    }
    return 0;
}

/*
 * @description		: 关闭/释放设备
 * @param - filp 	: 要关闭的设备文件(文件描述符)
 * @return 			: 0 成功;其他 失败
 */
static int led_release(struct inode *inode, struct file *filp)
{
    return 0;
}

/* 设备操作函数 */
static struct file_operations led_fops = {
        .owner = THIS_MODULE,
        .open = led_open,
        .read = led_read,
        .write = led_write,
        .release = 	led_release,
};

/*===============================2.平台驱动接口==================================*/
static int led_register_cdev(void)
{
	//1.申请设备号
	 if(leddev.major)
	 {
		 leddev.devid = MKDEV(leddev.major,0);
		 register_chrdev_region(leddev.devid,LEDDEV_CNT,LEDDEV_NAME);
	 }else{
		 alloc_chrdev_region(&leddev.devid,0,LEDDEV_CNT,LEDDEV_NAME);
		 leddev.major = MAJOR(leddev.devid);
	 }
	 
	 //2.初始化设备描述符
	 leddev.cdev.owner=THIS_MODULE;
	 cdev_init(&leddev.cdev,&led_fops);
	
	 //3.添加一个cdev
	 cdev_add(&leddev.cdev,leddev.devid,LEDDEV_CNT);
	
	 //4.创建类
	 leddev.class = class_create(THIS_MODULE,LEDDEV_NAME);
	 if(IS_ERR(leddev.class)){
		 return PTR_ERR(leddev.class);
	 }
	
	 //5.创建设备　
	 leddev.device = device_create(leddev.class,NULL,leddev.devid,NULL,LEDDEV_NAME);
	 if(IS_ERR(leddev.device)){
		 return PTR_ERR(leddev.device);
	 }

	 return 0;

}
static int led_probe(struct platform_device *dev)
{
	int i = 0;
	int ressize[5];
	struct resource *ledsource[5];

	printk("led driver and device has matched!\r\n");

	//1.获取资源
	for(i=0;i<5;i++)
	{
		ledsource[i] = platform_get_resource(dev,IORESOURCE_MEM,i);
		if(!ledsource[i])
		{
			dev_err(&dev->dev,"No MEM resource for always on\n");
			return -ENXIO;
		}
		ressize[i] = resource_size(ledsource[i]);
	}

	//2.初始化led
	_mygpio_init(ledsource,ressize);

	//3.注册字符设备驱动
	return  led_register_cdev();
}

static int led_remove(struct platform_device *dev)
{
   //1.释放io
   _myled_unmap();

   //2.注销设备资源
   cdev_del(&leddev.cdev);
   unregister_chrdev_region(leddev.devid,LEDDEV_CNT);
   device_destroy(leddev.class,leddev.devid);
   class_destroy(leddev.class);
   return 0;
}

/*platform驱动结构*/
static struct platform_driver led_driver = {

	.driver = {.name = "imx6ull-led"},
    .probe = led_probe,
    .remove = led_remove,
};


/*===============================3.模块驱动====================================*/
static int __init leddriver_init(void)
{
	return platform_driver_register(&led_driver);
}
static void __exit leddriver_exit(void)
{
	platform_driver_unregister(&led_driver);
}
module_init (leddriver_init);
module_exit(leddriver_exit);
MODULE_LICENSE("GPL");
