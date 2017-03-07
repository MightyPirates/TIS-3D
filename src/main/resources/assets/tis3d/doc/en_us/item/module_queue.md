# Queue Module

![Always push before pop](item:tis3d:module_queue)

The queue module is capable of storing up to sixteen (16) values. It acts as expanded memory for [execution modules](module_execution.md).

While not full, the stack module reads values from all four of its ports and pushes read values on top of the list of stored values. While not empty, the stack module writes the bottommost value, i.e. the value that was first pushed to the internal list of values, to all four of its ports.

A value on the stack can always only be transferred to one port, i.e. values will never be duplicated; even when multiple reads would occur in one [controller](../block/controller.md) cycle, only one will succeed. Whenever a value is stored on the stack, the stack module resets its write operations to ensure the correct value is being transferred. The specific timings resulting from this are vendor specific and an implementation detail.

The queue module acts as a 16 deep FIFO (first in, first out) buffer, for a FILO (first in, last out) buffer, refer to the [Stack Module](module_stack.md).
