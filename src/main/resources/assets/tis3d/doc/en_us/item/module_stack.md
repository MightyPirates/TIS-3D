# Stack Module

![Stack overflow](item:tis3d:module_stack)

The stack module is capable of storing up to sixteen (16) values. It acts as expanded memory for [execution modules](module_execution.md).

While not full, the stack module reads values from all four of its ports and pushes read values on top of the list of stored values. While not empty, the stack module writes the topmost value, i.e. the value that was last pushed to the internal list of values, to all four of its ports.

A value on the stack can always only be transferred to one port, i.e. values will never be duplicated; even when multiple reads would occur in one [controller](../block/controller.md) cycle, only one will succeed. Whenever a value is stored on the stack, the stack module resets its write operations to ensure the correct value is being transferred. The specific timings resulting from this are vendor specific and an implementation detail.

The stack module acts as a 16 deep FILO (first in, last out) buffer, for a FIFO (first in, first out) buffer, refer to the [Queue Module](module_queue.md).
