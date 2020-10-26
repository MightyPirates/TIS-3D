# 队列模块

![Queue这单词听上去一股英国那味……](item:tis3d:module_queue)

【注意】
此页面尚未完成翻译。您可以帮助我们在GitHub上翻译此内容。

队列模块能够储存多达**16**个值。它可用于扩充模块（比如[执行模块](module_execution.md)）的内存。

While not full, the queue module reads values from all four of its ports and pushes read values to the end of the list of stored values. While not empty, the queue module writes the head value, i.e. the value that was first pushed to the internal list of values, to all four of its ports. In other words, the queue module is FIFO buffer.

A value from the queue can always only be transferred to one port, i.e. values will never be duplicated; even when multiple reads would occur in one [controller](../block/controller.md) cycle, only one will succeed. Unlike the [stack module](module_stack.md), the queue module does not reset its write operations when a value is written to it.
