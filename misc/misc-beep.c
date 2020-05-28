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
#include <linux/of.h>
#include <linux/of_address.h>
#include <linux/of_gpio.h>
#include <linux/platform_device.h>
#include <linux/property.h>
#include <linux/miscdevice.h>
#include <asm/mach/map.h>
#include <asm/uaccess.h>
#include <asm/io.h>

/*设备树:
/ {
	 beep{
		compatible = "misc-beep";
	 	pinctrl-names = "default";
		pinctrl-0 = <&pinctrl_beep>;
		beep-gpio = <&gpio5 1 GPIO_ACTIVE_LOW>;
		default-state = "off";
		
	};
};
*/

#define MISCBEEP_NAME  "miscbeep"     //设备名
#define MISCBEEP_MINOR  144           //子设备号
#define BEEPOFF 0
#define BEEPON  1

//=================================1.beep 的 文件操作符====================//
struct miscbeep_dev{
	struct device_node *nd;  //设备节点 
	int beep_gpio;           //beep所使用的GPIO编号
};
struct miscbeep_dev miscbeep;

static int miscbeep_open(struct inode *inode,struct file *filp)
{
	//设置私有数据
	filp->private_data = &miscbeep;
	return 0;
}

static ssize_t miscbeep_write(struct file *filp, const char __user*buf,size_t cnt,loff_t *offt)
{
	int ret = 0;
	unsigned char databuf[1];
	unsigned char beepstat;
    struct miscbeep_dev  *dev =filp->private_data;

    //用户输入的参数
    ret=copy_from_user(databuf,buf,cnt);
	if(ret<0)
	{
		printk("kernel write failed!\r\n");
		return -EFAULT;
	}
	beepstat = databuf[0];

	//蜂鸣器操作
	if(beepstat == BEEPON)
		gpio_set_value(dev->beep_gpio,0);
	else if(beepstat == BEEPOFF)
		gpio_set_value(dev->beep_gpio,1);
	return 0;    
}

static struct file_operations miscbeep_fops={ //文件操作
	.owner = THIS_MODULE,
	.open  = miscbeep_open,
	.write = miscbeep_write,
};
static struct miscdevice beep_miscdev = {  //misc设备结构体
    .minor = MISCBEEP_MINOR,
	.name = MISCBEEP_NAME,                 //设备在 /sys/devices/virtual/misc/miscbeep
	.fops = &miscbeep_fops,
};
//=================================2.beep 的 platform驱动====================//
static int miscbeep_probe(struct platform_device *dev)
{
    
	printk("beep driver and device was matched!\r\n");

	/*1.根据设备树，配置beep*/
	if((miscbeep.nd = of_find_node_by_path("/beep"))==NULL)
	{
		printk("beep node not find!\r\n");
		return -EINVAL;
	}

	if((miscbeep.beep_gpio=of_get_named_gpio(miscbeep.nd,"beep-gpio",0))<0)
	{
		printk("can't get beep-gpio!\r\n");
		return -EINVAL;
	}

	if(gpio_direction_output(miscbeep.beep_gpio, 1)<0)//高电平，关闭
	{
		printk("can't set gpio!\r\n");
	}

	/*2.注册misc设备*/
	if(misc_register(&beep_miscdev)<0)
	{
		printk("misc device register failed!\r\n");
		return -EFAULT;
	}
	return 0;
}

static int miscbeep_remove(struct platform_device *dev)
{
	gpio_set_value(miscbeep.beep_gpio,0);//关闭beep
	misc_deregister(&beep_miscdev);      //注销beep
	return 0;
}

//=================================3.平台模块====================//
static const struct of_device_id beep_of_match[]={ //设备树节点 匹配列表
 {.compatible = "misc-beep"}                       //匹配的节点
};

static struct platform_driver beep_driver = {  //platform  驱动
	.driver = {
		.name = "my-misc-beep",                //驱动在 :/sys/bus/platform/drivers/my-misc-beep
		.of_match_table = beep_of_match,
	},
	.probe = miscbeep_probe,
	.remove = miscbeep_remove,
};

//模块注册
module_platform_driver(beep_driver);
MODULE_LICENSE("GPL"); //必须加入一个GPL


