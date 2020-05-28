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

/*�豸��:
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

#define MISCBEEP_NAME  "miscbeep"     //�豸��
#define MISCBEEP_MINOR  144           //���豸��
#define BEEPOFF 0
#define BEEPON  1

//=================================1.beep �� �ļ�������====================//
struct miscbeep_dev{
	struct device_node *nd;  //�豸�ڵ� 
	int beep_gpio;           //beep��ʹ�õ�GPIO���
};
struct miscbeep_dev miscbeep;

static int miscbeep_open(struct inode *inode,struct file *filp)
{
	//����˽������
	filp->private_data = &miscbeep;
	return 0;
}

static ssize_t miscbeep_write(struct file *filp, const char __user*buf,size_t cnt,loff_t *offt)
{
	int ret = 0;
	unsigned char databuf[1];
	unsigned char beepstat;
    struct miscbeep_dev  *dev =filp->private_data;

    //�û�����Ĳ���
    ret=copy_from_user(databuf,buf,cnt);
	if(ret<0)
	{
		printk("kernel write failed!\r\n");
		return -EFAULT;
	}
	beepstat = databuf[0];

	//����������
	if(beepstat == BEEPON)
		gpio_set_value(dev->beep_gpio,0);
	else if(beepstat == BEEPOFF)
		gpio_set_value(dev->beep_gpio,1);
	return 0;    
}

static struct file_operations miscbeep_fops={ //�ļ�����
	.owner = THIS_MODULE,
	.open  = miscbeep_open,
	.write = miscbeep_write,
};
static struct miscdevice beep_miscdev = {  //misc�豸�ṹ��
    .minor = MISCBEEP_MINOR,
	.name = MISCBEEP_NAME,                 //�豸�� /sys/devices/virtual/misc/miscbeep
	.fops = &miscbeep_fops,
};
//=================================2.beep �� platform����====================//
static int miscbeep_probe(struct platform_device *dev)
{
    
	printk("beep driver and device was matched!\r\n");

	/*1.�����豸��������beep*/
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

	if(gpio_direction_output(miscbeep.beep_gpio, 1)<0)//�ߵ�ƽ���ر�
	{
		printk("can't set gpio!\r\n");
	}

	/*2.ע��misc�豸*/
	if(misc_register(&beep_miscdev)<0)
	{
		printk("misc device register failed!\r\n");
		return -EFAULT;
	}
	return 0;
}

static int miscbeep_remove(struct platform_device *dev)
{
	gpio_set_value(miscbeep.beep_gpio,0);//�ر�beep
	misc_deregister(&beep_miscdev);      //ע��beep
	return 0;
}

//=================================3.ƽ̨ģ��====================//
static const struct of_device_id beep_of_match[]={ //�豸���ڵ� ƥ���б�
 {.compatible = "misc-beep"}                       //ƥ��Ľڵ�
};

static struct platform_driver beep_driver = {  //platform  ����
	.driver = {
		.name = "my-misc-beep",                //������ :/sys/bus/platform/drivers/my-misc-beep
		.of_match_table = beep_of_match,
	},
	.probe = miscbeep_probe,
	.remove = miscbeep_remove,
};

//ģ��ע��
module_platform_driver(beep_driver);
MODULE_LICENSE("GPL"); //�������һ��GPL


