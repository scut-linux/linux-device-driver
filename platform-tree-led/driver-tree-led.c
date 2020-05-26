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

//设备树配置如下:
/*
/ {
	 pmyled{
	 	#address-cells = <1>;
		#size-cells = <1>;
		compatible = "pmyled";
	 	pinctrl-names = "default";
		pinctrl-0 = <&pinctrl_pmyled>;
		gpio = <&gpio1 3 GPIO_ACTIVE_LOW >; //gpio1 03
		status = "okay";
		
	};
};
*/


/*===============================1.设备驱动====================================*/

/*===============================================================================
 * GPIO1_IO03作为led
 * 时钟 IPG_CLK, 时钟控制　CCGR1
 * Pad 对应的是Soc芯片上的引脚
 * Mux 利用控制器
 * ===============================================================================
 * */
#define LEDDEV_CNT 1            /*设备号长度*/
#define LEDDEV_NAME      "dtsplatled"   /*设备名字*/
#define LEDOFF 	0				/* 关灯 */
#define LEDON 	1				/* 开灯 */

//leddev 设备结构体
struct leddev_dev{
	dev_t devid; //设备号
	struct cdev cdev; //cdev
	struct class *class; //类
	struct device* device;//设备
	int major;     //主设备号	
	struct device_node *node; //led设备节点
	int led0;      //led灯的gpio标号 
};
struct leddev_dev leddev;  //led 设备实例


//初始化IO
static int _mygpio_init(void)
{
	leddev.node = of_find_node_by_path("/pmyled");
	if(leddev.node == NULL)
	{
		printk("gpioled node can not find!\r\n");
		return -EINVAL;
	}
	leddev.led0 = of_get_named_gpio(leddev.node, "gpio", 0);
	if(leddev.led0<0)
	{
		printk("can't get led-gpio\r\n");
		return -EINVAL;
	}
    gpio_request(leddev.led0,"led0");
	gpio_direction_output(leddev.led0,1);//设置输出
	return 0;
	
}

//io设置值
void _myled_switch(u8 sta)
{
    if(sta == LEDON) {
		gpio_set_value(leddev.led0,0);
    }else if(sta == LEDOFF) {
		gpio_set_value(leddev.led0,1);
    }
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
   filp->private_data = &leddev;//私有数据
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


/* 设备操作函数 */
static struct file_operations led_fops = {
        .owner = THIS_MODULE,
        .open = led_open,
        .read = led_read,
        .write = led_write,
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
	int ret =0;
	printk("led driver and device has matched!\r\n");

	//1.注册字符设备驱动
	ret = led_register_cdev();
	if(ret<0)return ret;

	//2.初始化led
	ret = _mygpio_init();
	if(ret<0)return ret;

	return 0;
}

static int led_remove(struct platform_device *dev)
{
   //1.关闭led
   gpio_set_value(leddev.led0,1);

   //2.注销设备资源
   cdev_del(&leddev.cdev);
   unregister_chrdev_region(leddev.devid,LEDDEV_CNT);
   device_destroy(leddev.class,leddev.devid);
   class_destroy(leddev.class);
   return 0;
}

/*设置树配置列表*/
static const struct of_device_id led_of_match[]={
 {.compatible = "pmyled"},
};

/*platform驱动结构*/
static struct platform_driver led_driver = {

	.driver = {
			.name = "imx6ull-tree-led",
			.of_match_table = led_of_match,
	},
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

//modprobe driver-tree-led.ko
//测试./platform-led /dev/dtsplatled  1