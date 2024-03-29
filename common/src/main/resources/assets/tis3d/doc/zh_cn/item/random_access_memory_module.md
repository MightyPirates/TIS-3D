# 随机存取存储器模块
![不参加选秀.](item:tis3d:random_access_memory_module)

随机存取存储器模块(RAM模块)允许存储一个庞大的总供以后检索256位值。每个所存储的值可以单独访问，因此而得名。请注意，与TIS-3D计算机本身令人印象深刻的16 位值的支持，内存模块限定为8位值。

内存模块遵循一个简单的协议，用于读取和写入单个单元：它会先读取任何端口的单个值。此值定义了在读或写操作将被执行的**地址**。提供了超过8位宽度的值将导致 未定义行为。
- 当RAM模块的端口中的任何一个完成了一个读出操作时 ，读值将被存储在该地址。
- 当RAM模块的端口中的任何一个完成写操作时，存储在 该地址中的值将被转移。无论输入的地址是被读取或写入之后，在RAM模块将返回读取另一地址以使下一个读或写操作。

例如读取左边的随机存取存储器模块地址`8`的内容到 ACC：
`MOV 8 LEFT`
`MOV LEFT ACC`

例如，对于写`0x42`到地址`8`，同样的方式：
`MOV 8 LEFT`
`MOV 0x42 LEFT`