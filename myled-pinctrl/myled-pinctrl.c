#include <linux/types.h>
#include <linux/kernel.h>
#include <linux/delay.h>
#include <linux/ide.h>
#include <linux/init.h>
#include <linux/module.h>
#include <linux/errno.h>
#include <linux/gpio.h>
#include <asm/mach/map.h>
#include <asm/uaccess.h>
#include <asm/io.h>

#include <linux/cdev.h>
#include <linux/device.h>



#include <linux/of.h>
#include <linux/of_address.h>
#include <linux/of_gpio.h>

/*================================================================================
	linux 已经集成了统一的操作 gpio接口 : pinctrl + gpio子系统 
	通过配置设备树，调用对应的接口就可以使用

	pintrl 子系统 功能:  1.pin信息 2.pin复用功能 3.pin的电气特性(上拉/下拉 速度 驱动能力..)
	gpio   子系统 功能:1.初始化          2.读写pin

1.设备树配置

 / {
	  pmyled{
	 	#address-cells = <1>;
		#size-cells = <1>;
		 compatible = "pmyled";
		 pinctrl-names = "default";
		 pintctrl-0 = <&pinctrl_pmyled>;
		 gpio = <&gpio1 3 GPIO_ACTIVE_LOW >; //gpio1 03
		 status = "okay";
		 
	 };
 };
 
 &iomuxc ->imx6ul-evk 节点中添加
 
		pinctrl_pmyled:pmyled{
			 fsl,pins = <
		 MX6UL_PAD_GPIO1_IO03__GPIO1_IO03
			 >;
		 };
		 
2.驱动代码 
	
 * ===============================================================================
 * */

#define GPIOLED_CNT 1
#define GPIOLED_NAME  "pmyled"
#define LEDOFF 1
#define LEDON  0

/*gpio led 设备结构体*/
struct gpioled_dev{
	dev_t devid;
	struct cdev cdev;          //字符设备
	struct class *class;       //类
	struct device *device;     //设备
	int major;                 //主设备号
	int minor;                 //次设备号
	struct device_node *nd;    //设备节点
	int led_gpio;              //led 的 gpio编号	
};

static struct gpioled_dev gpioled;


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
	filp->private_data = &gpioled; /*设置私有数据*/
    return 0;
}

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
	struct gpioled_dev *dev = filp->private_data;

    retvalue = copy_from_user(databuf, buf, cnt);//数据不能直接从用户空间传到内核空间，必须使用copy_from_user
    if(retvalue < 0) {
        printk("kernel write failed!\r\n");
        return -EFAULT;
    }

    ledstat = databuf[0];		/* 获取状态值 */

    if(ledstat == LEDON) {
        //_myled_switch(LEDON);		/* 打开LED灯 */
        gpio_set_value(dev->led_gpio,LEDON);
    } else if(ledstat == LEDOFF) {
        //_myled_switch(LEDOFF);	/* 关闭LED灯 */
        gpio_set_value(dev->led_gpio,LEDOFF);
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


/*===============================================================================
    模块接口
 * =============================================================================*/
static int myled_init(void) {
    printk("myled_init...\n");


   /*根据设备树设置gpio*/
   if( (gpioled.nd=of_find_node_by_path("/pmyled")) == NULL ){
		printk("/pmyled node not found!\r\n");
		return -EINVAL;
   }else{
		printk("/pmyled node found!\r\n");
   }
   //gpio配置 gpio1 03 low
   if((gpioled.led_gpio=of_get_named_gpio(gpioled.nd, "gpio", 0)) < 0){
		printk("can't get property:gpio\r\n");
		return -EINVAL;	
   }else{
		printk("pmyled gpio=%d\r\n",gpioled.led_gpio);
   }
   //设置输出
   if(gpio_direction_output(gpioled.led_gpio, LEDOFF)<0){
		printk("can't set gpio = off\r\n");
   }

	

    //1.申请设备号
    alloc_chrdev_region(&gpioled.devid,0,1,"pmyled");
    gpioled.major=MAJOR(gpioled.devid);
    gpioled.minor=MINOR(gpioled.devid);
    printk("major=%d,minor=%d\n",gpioled.major,gpioled.minor);

    //2.初始化设备描述符
    gpioled.cdev.owner=THIS_MODULE;
    cdev_init(&gpioled.cdev,&led_fops);

    //3.添加一个cdev
    cdev_add(&gpioled.cdev,gpioled.devid,1);

    //4.创建类
    gpioled.class = class_create(THIS_MODULE,"pmyled");
    if(IS_ERR(gpioled.class)){
        return PTR_ERR(gpioled.class);
    }

    //5.创建设备　
    gpioled.device = device_create(gpioled.class,NULL,gpioled.devid,NULL,"pmyled");
    if(IS_ERR(gpioled.device)){
        return PTR_ERR(gpioled.device);
    }

    return 0;
}
static void myled_exit(void) {
   
	unregister_chrdev(gpioled.major,"myled");//注销设备
	device_destroy(gpioled.class,gpioled.devid); //释放设备节点
	class_destroy(gpioled.class);//释放设备类
    printk("myled_exit...\n");
}

module_init (myled_init);
module_exit(myled_exit);
MODULE_LICENSE("GPL");
