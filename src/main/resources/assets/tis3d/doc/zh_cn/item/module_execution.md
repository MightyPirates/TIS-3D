# 执行模块

![执行——!](item:tis3d:module_execution)

【注意】
此文档已严重过期，因而不包含英文文档中更新的内容（或其翻译，若有原文搬运）。更新的内容包括多个指令。请查阅英文版以获取更多信息。

执行模块是编程TIS-3D计算机的主要手段。当此模块被安装到[外壳](../block/casing.md)中时，可以通过对其使用书来对此模块进行编程。如果需要更方便的代码编辑体验，你可以考虑来本[编程圣经](book_code.md)（又叫编程圣经），被任何所有专业人士认可的代码编辑神器。用一本普通的书使用在已安装的[执行模块](module_execution.md)上，可以得到编码书。

## 架构
执行模块允许用户以极大灵活性控制TIS-3D计算机的操作。每个执行模块可使用原始汇编语言进行编程。活动时，从程序中的第一条指令，逐个处理编程指令。完成其操作之后指示前进程序计数器将执行下一个指令。如果程序计数器离开指令的有效范围内(最后一条)，程序会自动从第一条指令执行。**这直接意味着程序循环**，除非明确地使其停机。

指令可以在不同种类的目标操作。所谓目标，是读值从，或写值到的一个有效来源。有效目标包括执行模块的四个端口，执行模块寄存器和少量的虚拟寄存器和端口。

一台TIS-3D计算机在执行模块，支持16位的值范围，符号值范围为-32768至32767。*由于技术上的限制，一些模块，包括执行模块处理，可选择显示数值为无符号十六进制*。

## 目标
`ACC`

类型：内部

描述：ACC是一个基本执行节点的首要的存储寄存器。ACC用作内源或多条指令的目标操作数，包括算术和条件指令。

`BAK`

类型：内部（不可寻址）

描述：BAK是ACC值的临时存储单元。只能通过SAV和SWP指令控制它，而且不能直接读写。

`NIL`

类型：内部（特殊）

描述：读入NIL即读入0。写入NIL没有任何效果。NIL可以作为目标操作数，使得该指令只产生部分效果，结果会被抛弃。

`LEFT, RIGHT, UP, DOWN`

类型：接口

描述：4个寄存器UP, DOWN, LEFT, 和RIGHT分别对应于所有基本执行节点与相邻节点通信的4个接口【译者注：分别对应上、下、左、右】。在硬件方面，有些接口会与特定节点断开，这会永久阻碍读入或写出命令的执行。参看节点的接线图可以确定哪些接口可以使用。

`ANY`

类型：接口（伪接口）

描述：当ANY作为一条指令的来源时，指令会读入第一个能够读取的接口的数值。当ANY作为一条指令的目标时，这条指令输出的结果会传递给第一个从本节点通过任何接口读入数据的节点。
【译者注：经本人实验，从ANY读入时，如果同时有多个接口输入，优先级是：左>右>上>下；向ANY输出时，如果同时有多个接口可以输出，优先级是上>左>右>下】

`LAST`

类型：接口（伪接口）

描述：LAST指最近用ANY伪接口读入或写出的接口。如若不然，它的效果和明确指定一个接口是相同的。在使用ANY伪接口成功读入或写出而使得LAST被确定之前，从LAST读入或向LAST写出的结果是实现定义的行为。

## 指令集

In addition to a list of instructions, assembler code provided to an execution module may contain metadata. Comments are textual notes in the code that are completely ignored in the execution of the program. Labels mark positions in the code that can be addressed by jump instructions. Comments, labels and blank lines have no influence on the addressing of the compiled program. This is relevant when using the `JRO` instruction.

*SRC*和*DST*指令参数【译者注：SRC指source（来源），DST指destination（目标）】可以指定一个接口或内部寄存器。使用任何接口的时候，在连接该接口的对应节点完成读写通信之前，指令会停在这里。另外，*SRC*参数也可以是-999到999之间的字面值整数。【译者注：也就是说你可以在这里直接输入一个整数】

BAK既不是*SRC*也不是*DST*。BAK的值只有通过特殊的指令SAV和SWP来访问。*LABEL*参数是任意的文本名称，用于在程序中标记跳跃的目标位置。

### 注释

句法：#注释文本

描述：所有#和之后的文本会被程序忽略。

样例： 
`# Single line comment`  
`LOOP: # Start of loop`  
`MOV 0, ACC # Reset`

### Labels（标签）

句法：*LABEL*:

描述：标签用于确定跳跃指令的目标位置。当程序跳跃到该目标时，此标签后面的指令会接着执行。

样例：

LOOP: 本标签独占一行。

L: MOV 8, ACC 标签L和一条指令在同一行。

2-3. NOP

句法：NOP

等效指令：ADD NIL

描述：NOP是一条伪指令，对于节点内部的状态和通信接口都无任何影响。NOP会被程序自动替换为`ADD NIL`。

2-4.MOV

句法：MOV *SRC*, *DST*

描述：读入*SRC*，将结果写入*DST*。【译者注：MOV指move（移动）】

样例：

`MOV 8, ACC` 将字面值8写入ACC。

`MOV LEFT, RIGHT` 从接口LEFT读入，写入RIGHT。

`MOV UP, NIL` 从接口UP读入，抛弃该结果。

2-5. SWP

句法：SWP

描述：将ACC和BAK的值交换。【译者注：SWP指swap（交换）】

2-6. SAV

句法：SAV

描述：将ACC的值写入BAK。【译者注：SAV指save（保存）】

2-7. ADD

句法：ADD *SRC*

描述：将ACC的值加上*SRC*的值，结果存入ACC。

样例：

`ADD 16` ACC的值加上字面值16。

`ADD LEFT` ACC的值加上从LEFT接口读入的值。

2-8. SUB

句法：SUB *SRC*

描述：将ACC的值减去*SRC*的值，结果存入ACC。【译者注：SUB指subtract（减）】

样例：

`SUB 16` ACC的值减去字面值16。

`SUB LEFT` ACC的值减去从LEFT接口读入的值。

2-9. NEG

句法：NEG

描述：ACC的值取反。0保持不变。【译者注：NEG指negative（取负数）】

2-10. JMP

句法：JMP *LABEL*

描述：执行无条件转移。跳到标签*LABEL*后的语句接着执行。【译者注：JMP指jump（跳跃）】

2-11. JEZ

句法：JEZ *LABEL*

描述：执行有条件转移。如果ACC的值是0，跳到标签*LABEL*后的语句接着执行。【译者注：JEZ指jump equal zero（等于0就跳）】

2-12. JNZ

句法：JNZ *LABEL*

描述：执行有条件转移。如果ACC的值不是0，跳到标签*LABEL*后的语句接着执行。【译者注：JNZ指jump not zero（不是0就跳）】

2-13. JGZ

句法：JGZ *LABEL*

描述：执行有条件转移。如果ACC的值是正数（大于0），跳到标签*LABEL*后的语句接着执行。【译者注：JGZ指jump greater than zero（大于0就跳）】

2-14. JLZ

句法：JLZ *LABEL*

描述：执行有条件转移。如果ACC的值是负数（小于0），跳到标签*LABEL*后的语句接着执行。【译者注：JLZ指jump less than zero（小于0就跳）】

2-15. JRO

句法：JRO *SRC*

描述：执行无条件转移。跳到由该行往后数第*SRC*行接着执行。【译者注：JRO指jump relative offset（跳到相关行）】

样例：

`JRO 0` 重复执行本行语句，效果是程序停止。

`JRO -1` 回到上一行执行。

`JR0 2` 向后隔一行执行。

`JRO ACC` 根据ACC的值跳转执行。
