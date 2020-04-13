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
/*===============================================================================
 * GPIO1_IO03作为led
 * 时钟 IPG_CLK, 时钟控制　CCGR1
 * Pad 对应的是Soc芯片上的引脚
 * Mux 利用控制器
 * ===============================================================================
 * */
#define LEDOFF 	0				/* 关灯 */
#define LEDON 	1				/* 开灯 */

#define GPIO1_DR_BASE               (0x0209C000)
#define GPIO1_GDIR_BASE             (0x0209C004)
#define CCM_CCGR1_BASE              (0x020C406C)
#define SW_MUX_GPIO1_IO03_BASE		(0X020E0068)
#define SW_PAD_GPIO1_IO03_BASE		(0X020E02F4)

//虚拟内存地址
static void __iomem *IMX6U_CCM_CCGR1;
static void __iomem *SW_MUX_GPIO1_IO03;
static void __iomem *SW_PAD_GPIO1_IO03;
static void __iomem *GPIO1_DR;
static void __iomem *GPIO1_GDIR;
//初始化IO
static void _mygpio_init(void)
{
    u32 val = 0;

    /* 初始化LED */
    /* 1、寄存器地址映射 */
    IMX6U_CCM_CCGR1 = ioremap(CCM_CCGR1_BASE, 4);
    SW_MUX_GPIO1_IO03 = ioremap(SW_MUX_GPIO1_IO03_BASE, 4);
    SW_PAD_GPIO1_IO03 = ioremap(SW_PAD_GPIO1_IO03_BASE, 4);
    GPIO1_DR = ioremap(GPIO1_DR_BASE, 4);
    GPIO1_GDIR = ioremap(GPIO1_GDIR_BASE, 4);

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


/*===============================================================================
    模块接口
 * =============================================================================*/
//static int myled_init(void) {
//    int result=0;
//    printk("myled_init...\n");
//    init_mygpio(); //初始化IO
//    result = register_chrdev(201,"myled",&led_fops);//注册设备
//    if(result < 0){
//        printk("register chrdev failed!\r\n");
//        return -EIO;
//    }
//    return 0;
//}

#include <linux/cdev.h>
#include <linux/device.h>

static  dev_t devid;//设备号
static int major;
static int minor;
struct cdev cdev;
struct device *device;
struct class *class;
static int myled_init(void) {
    printk("myled_init...\n");
    _mygpio_init(); //初始化IO

    //1.申请设备号
    alloc_chrdev_region(&devid,0,1,"myled");
    major=MAJOR(devid);
    minor=MINOR(devid);
    printk("major=%d,minor=%d\n",major,minor);

    //2.初始化设备描述符
    cdev.owner=THIS_MODULE;
    cdev_init(&cdev,&led_fops);

    //3.添加一个cdev
    cdev_add(&cdev,devid,1);

    //4.创建类
    class = class_create(THIS_MODULE,"myled");
    if(IS_ERR(class)){
        return PTR_ERR(class);
    }

    //5.创建设备　
    device = device_create(class,NULL,devid,NULL,"myled");
    if(IS_ERR(device)){
        return PTR_ERR(device);
    }

    return 0;
}
static void myled_exit(void) {
    
	_myled_unmap(); //释放io
	unregister_chrdev(major,"myled");//注销设备
	device_destroy(class,devid); //释放设备节点
	class_destroy(class);//释放设备类
    printk("myled_exit...\n");
}

module_init (myled_init);
module_exit(myled_exit);
MODULE_LICENSE("GPL");

