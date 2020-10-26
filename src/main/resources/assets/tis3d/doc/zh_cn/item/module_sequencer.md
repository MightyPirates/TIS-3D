# 序列器模块

![你能重复一遍吗？](item:tis3d:module_sequencer)

【注意】
此页面尚未完成翻译。您可以帮助我们在GitHub上翻译此内容。

The sequencer module emits a sequence of eight values in a loop. These eight values can be configured on the module by interacting with it after it has been installed in a [casing](../block/casing.md). Each column represents a sequence value, each row represents a bit of these values. The topmost row controls the first bit (1), the bottommost the highest (128).

Each time the sequence advances, including when it wraps around, the sequencer module will abort any incomplete previous writes and begin to write the value of the current column to all four of its ports exactly *once*. Any secondary read operations will block until the sequence advances again.

The sequencer module continuously reads from all four of its ports. When a value is read, the delay in [controller](../block/controller.md) cycles between sequence advancement is set to that value. When set to zero, the sequence will advance each cycle. The default value is vendor specific.

Module configuration, including delay of sequence advancement, is persisted across reboots. The sequence will begin from the first column after a reboot. Configuration will be lost when a sequencer module is removed from its casing.
