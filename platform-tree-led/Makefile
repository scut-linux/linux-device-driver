KERNELDIR := $(ARM_LINUX_KERNEL_HOME)
CURRENT_PATH := $(shell pwd)

obj-m :=driver-tree-led.o

build: kernel_modules

kernel_modules:
	$(MAKE) -C $(KERNELDIR) M=$(CURRENT_PATH) modules

clean:
	$(MAKE) -C $(KERNELDIR) M=$(CURRENT_PATH) clean
