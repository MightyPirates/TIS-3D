# 熔炉
![有什么烧东西的气味...](block:minecraft:furnace)

利用串口模块没有被证明的光敏感性，计算机可以访问普通熔炉处理过程中的一些基本信息。本协议支持读操作和写操作。

当使用串口模块向熔炉写入时，可以使用两个值：`0`和`1`。提供其他的值将导致未定义行为。`0`将返回剩余燃料 信息，`1`将返回当前熔炼或烹饪进度。

读取熔炉信息时，会返回一个在[0, 100]范围之间的数字。 当模式在返回剩余燃料信息时，0表示没有燃料，100表示燃料是满的。这个值是相对于当前燃料的燃烧时间。它与燃料留在炉内的量无关。当模式在返回进度时，0意味着没有进度，100意味着完成，且即将结束。
