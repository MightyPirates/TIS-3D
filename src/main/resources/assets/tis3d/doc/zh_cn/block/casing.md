# 外壳

![In case of logic](item:tis3d:casing)

【注意】
此页面尚未完成翻译。您可以帮助我们在GitHub上翻译此内容。

外壳可以安装六个模块。有鉴于外壳必须要连接到[控制器](controller.md)才能发挥作用，所以通常只能连接五个；外壳与外壳之间，或外壳与控制器之间的面不能连接模块，因为这些面需要用来进行内部通讯。在装有模块的面旁边放置外壳或控制器将导致模块被强行弹出。

外壳可以为每个模块传送四个端口。边缘方向也是如此。

这意味着外壳可以读取到对面模块的端口。然而，如果没有模块安装在这样的插槽，会导致它不会成功地从端口读取，也不会写。

Casings can be locked using a [key](../item/key.md). Once locked, modules can no longer be added or removed. Useful for preventing manipulation by others, or simply to prevent accidentally removing modules.

Furthermore, while sneaking, [keys](../item/key.md) can be used to close/open the receiving ports of each casing's faces. Closing a receiving port on a face will cause writing operations to that port from the adjacent module to stall, and will prevent omnidirectional writes to output to the port. This allows for more compact builds, and can save you a few [execution modules](../item/module_execution.md) otherwise needed for making a connection directional (e.g. only forwarding received data from an [infrared module](../item/module_infrared.md) to a [redstone module](../item/module_redstone.md)).