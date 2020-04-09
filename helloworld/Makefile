<<<<<<< HEAD
KERNELDIR := $(ARM_LINUX_KERNEL_HOME)
CURRENT_PATH := $(shell pwd)

obj-m := helloworld.o

build: kernel_modules

kernel_modules:
	$(MAKE) -C $(KERNELDIR) M=$(CURRENT_PATH) modules

clean:
	$(MAKE) -C $(KERNELDIR) M=$(CURRENT_PATH) clean
=======
# To build modules outside of the kernel tree, we run "make"
# in the kernel source tree; the Makefile these then includes this
# Makefile once again.
# This conditional selects whether we are being included from the
# kernel Makefile or not.

# called from kernel build system: just declare what our modules are
obj-m := helloworld.o


CROSS_COMPILE = arm-linux-gnueabihf-
CC            = $(CROSS_COMPILE)gcc

# Assume the source tree is where the running kernel was built
# You should set KERNELDIR in the environment if it's elsewhere
#KERNELDIR ?= /usr/src/linux-headers-$(shell uname -r)
KERNELDIR = $(ARM_LINUX_KERNAL_HOME)
# The current directory is passed to sub-makes as argument
PWD := $(shell pwd)

all:modules

modules:
	$(MAKE) -C $(KERNELDIR) M=$(PWD)  modules

clean:
	rm -rf  *.o  *.order  *.symvers  *.ko  *.mod.c   *.mod.o  .tmp_versions .helloworld*
>>>>>>> 65d893744f15b6f29a86a1e2fb002b6018099319
