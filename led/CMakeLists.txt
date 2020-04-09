cmake_minimum_required(VERSION 3.15)
project(led)

set(CMAKE_C_STANDARD 99)
include_directories(/home/kaihua/Desktop/build/myimx6/include)
include_directories(/home/kaihua/Desktop/build/myimx6/arch/arm/include)
add_executable(led led.c)