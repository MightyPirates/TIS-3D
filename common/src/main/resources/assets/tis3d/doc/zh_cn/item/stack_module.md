# 栈模块
![栈溢出](item:tis3d:stack_module)

堆栈模块能够存储多达**16**个值。它作为扩充[执行模块](execution_module.md)的内存。

栈遵循后进先出原则，也就是说，最先入栈的值总是最后取出来，而最后入栈的值总是最先取出来。

栈上的一个值始终仅可传送到一个端口，即值将永远不会被复制；即使在一个[控制器](../block/controller.md)周期中出现多个读取操作时，也仅仅会有一次成功。每当一个值存储在栈上，堆栈模块将它的写入进行操作，以确保正确的值正在转移。其实际操作由具体实现决定，因制造商或销售商而异。
