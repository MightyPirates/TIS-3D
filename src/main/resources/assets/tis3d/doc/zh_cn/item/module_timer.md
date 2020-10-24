# 计时器模块

![嗯，是时候了](item:tis3d:module_timer)

【注意】
译者从英文文档翻译下文中的内容时感觉其艰涩难懂。为准确起见，以下以保留有疑问的英文原文。您可以帮助我们在GitHub上改进这些翻译。

计时器模块使用高精度石英谐振以提供一致且可靠的定时等待操作。其硬件运行于20Hz的时钟上，意味着计时器每步进1次需要花费刚刚好50毫秒。计时器使用以下时钟周期进行配置。

The timer module uses a high precision quartz to allow consistent and reliably timed wait operations. Its hardware runs on a 20 Hz clock, meaning one timer step will take exactly 50 milliseconds. The timer is configured using these clock cycles.

计时器模块持续不断地从所有四个端口读取值。读取值时，内部计时器状态被设置为指定的值，每个时钟周期后会自减1，直到其值被减为0。一旦该值到达0，计时器模块会向四个端口写入持续的，“实现特定”的值。

The timer module continuously reads values from all four of its ports. When a value is read, the internal timer state is set to the specified value, and will be decremented by one in each future clock cycle, until it reaches zero. Once it has reached zero, the timer module continuously writes a constant, implementation specific value to all four of its ports.



As the timer module only writes to its ports when the timer has elapsed, it is therefore possible to wait for a specific amount of time, by setting up the timer and then trying to read a value from it. Due to the blocking nature of port I/O in TIS-3D computers, this will pause exectution until the timer has elapsed.

An interrupt may be implemented by having an [execution module](module_execution.md) read from the timer module using the virtual `ANY` port, therefore allowing concurrent programs to end the read by pushing a value to that [execution module](module_execution.md).

## 时间转换表
简而言——咳咳，简单起见，我们在这里准备了一份常用的时间值对应表。以下为计时器模块必须的用于配置其等待时间的时间表。

For the simpl- for simplicity, we provide a lookup table of commonly used times to the value the timer module has to be configured with to wait for that amount of time.

0.5秒： 10
1秒： 20
5秒： 100
10秒： 200
1分钟： 1200
15分钟： 18000
30分钟： 36000

计时器的最大值由TIS-3D计算机的端口带宽所决定，即16位。因此，最大的可配置计时器值为`0xFFFF`，对应54分36秒750毫秒。

The maximum timer value is limited by the bandwidth of the ports in TIS-3D computers, which is 16 bit. Therefore, the maximum configurable timer value is 0xFFFF, which results in a wait of 54 minutes 36 seconds and 750 milliseconds.