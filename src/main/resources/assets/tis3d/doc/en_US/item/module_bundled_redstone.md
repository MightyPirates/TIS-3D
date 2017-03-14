# Bundled Redstone Module

![16 in 1](item:tis3d:module_bundled_redstone)

The bundled redstone module provides TIS-3D computers a means to interface with bundled redstone implementations from various manufacturers. It will interface with *only* bundled cables. For the various insulated cables vendors regularly provide, please refer to the regular [redstone module](module_redstone.md). The bundled redstone module requires a slightly more advanced protocol due to multiple channels being interfaced. Like the regular [redstone module](module_redstone.md) it allows both reading and emitting signals. Note that the bundled redstone module will not perform any signal processing such as normalization or clamping. Illegal values will lead to undefined behavior. Refer to your bundled cables' vendor's specification on legal values.

The bundled redstone module has a concept of an active channel. This determines from which bundled channel values are read. The channel can be changed by providing an appropriately formatted value (see *Signal Specification*). The bundled redstone module reads values from all four of its ports and emits a signal of the encoded strength to the encoded channel (see *Signal Specification*). The bundled redstone module writes the redstone signal currently applied to the active channel to all four of its ports.

## Signal Specification
The values the bundled redstone module reads from its ports are interpreted as two separate values, one in the high byte (`0xFF00`) of the value and one in the low byte (`0x00FF`). The high byte typically denotes the channel to write the value in the low byte to. As the sole exception, if the high byte equals `0xFF` the low byte contains the channel to set the active channel to. In other words, to set the active channel, which determines which channel values are read from, the value must contain `0xFF` as the high byte, and the number of the new active channel as the low byte.

Example of how to switch a bundled redstone module on the left port of an execution module to the seventh channel:
`MOV 0XFF ACC`
`SHL 8 # HI = 0xFF`
`ADD 6 # LO = 6`
`MOV ACC LEFT`
