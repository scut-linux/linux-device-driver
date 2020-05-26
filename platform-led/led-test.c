#include "stdio.h"
#include "unistd.h"
#include "sys/types.h"
#include "sys/stat.h"
#include "fcntl.h"
#include "stdlib.h"
#include "string.h"

#define LEDOFF 	0				/* 关灯 */
#define LEDON 	1				/* 开灯 */


int main(int argc,char *argv[])
{
	int fd,ret;
	char *filename;
	unsigned char databuf[2];

	if(argc !=3)
	{
	  printf("Error Usage!\r\n");
	  return -1;
	}

	filename = argv[1];

	fd = open(filename,O_RDWR);
	if(fd <0){
		printf("file %s open failed!\r\n",argv[1]);
		return -1;
	}

	databuf[0] = atoi(argv[2]); //开关
	ret = write(fd,databuf,sizeof(databuf));
	if(ret <0)
	{
		printf("LED Control Failed!\r\n");
		return -1;
	}

    //关闭文件
    ret = close(fd);
	if(ret<0){
		printf("file %s close failed!\r\n",argv[1]);
		return -1;
	}

	return 0;
	
}