#include "stdio.h"
#include "unistd.h"
#include "sys/types.h"
#include "sys/stat.h"
#include "fcntl.h"
#include "stdlib.h"
#include "string.h"
 #include <unistd.h>

#define BEEPOFF 0				
#define BEEPON 	1			


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

	databuf[0] = atoi(argv[2]);
	ret = write(fd,databuf,sizeof(databuf));
	usleep(20000);
	ret = write(fd,BEEPOFF,1);
	if(ret <0)
	{
		printf("BEEP Control Failed!\r\n");
		return -1;
	}

    //¹Ø±ÕÎÄ¼þ
    ret = close(fd);
	if(ret<0){
		printf("file %s close failed!\r\n",argv[1]);
		return -1;
	}

	return 0;
	
}