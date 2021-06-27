# TIS-3D 参考手册
Tessellated Intelligence System是一个大规模并行计算机 架构，由非均匀的互联的异质节点构成。
Tessellated Intelligence System对于那些需要处理复杂数 据流的应用来说十分合适，如自动金融贸易、大数据收集、以及公民行为分析。
———— *TIS-100参考手册*

TIS-3D是个三维的TIS系统。它的目的是控制Minecraft世 界中的各种原本需要复杂红石操纵的机器和其它机制。或者说，这只是个有趣的挑战！

## 计算机详述
借助TIS-3D可构建强大的模块化计算机。该计算机包括 一台[控制器](block/controller.md)和最多八个[外壳](block/casing.md)。[外壳](block/casing.md)需要与控制器连接在 一起。注意，与[控制器](block/controller.md)的连接是可以传递的：也就是说， 若[外壳](block/casing.md)`C1`连接到了一台[控制器](block/controller.md)上，[外壳](block/casing.md)`C2`连接到了[外壳](block/casing.md)`C1`上，那么`C2`也会自动与这个[控制器](block/controller.md)相连。

计算机有且只能有一台[控制器](block/controller.md)。如果有其它[控制器](block/controller.md)直接或间接地连接，计算机就无法启动。一台计算机如果有超过八个[外壳](block/casing.md)，计算机也无法启动。

给[控制器](block/controller.md)在任何面上提供红石信号以启动计算机。计算机的运行速度由红石信号强度决定。若同时为[控制器](block/controller.md)给予多个红石信号会导致未定义行为。请联系[控制器](block/controller.md)制造商以获取整套行为规范，因为提供多个红石信号的行为可能会导致保修失效。提供最小的红石信号将暂停计算机。关闭计算机将完全重置其状态。

## 模块详述
[外壳](block/casing.md)可以安装六个[模块](item/index.md)，每一个都可以被放置在[外壳](block/casing.md)的侧面。每个[模块](item/index.md)有四个端口，分别为：`UP`、`RIGHT`、`down`、`left`，即上右下左。这些端口可通过[模块](item/index.md)读取和 写入值来进行通信。

每个读写操作会阻塞操作。[模块](item/index.md)通常在执行操作时加锁，直到操作完成后才会释锁。特定的例外可能存在。两个[模块](item/index.md)在同一共享端口上读操作，会导致它们陷入死锁，进而无法工作。如果[模块](item/index.md)写入一个端口上的一侧，没有其他[模块](item/index.md)，他们也会无法工作。重启计算机可解决这个问题。 对[模块](item/index.md)进行热交换在技术上是可行的，但通常会导致不可预知的结果。