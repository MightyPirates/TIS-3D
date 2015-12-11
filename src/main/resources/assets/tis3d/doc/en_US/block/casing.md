# Casing

![In case of logic](item:tis3d:casing)

The casing block houses up to six (6) modules, one on each of its faces. Because a casing must be connected to a [controller](controller.md) to function, generally only up to five (5) faces per module are usable; faced between casings or between a casing and a [controller](controller.md) are required for internal communication and cannot contain modules. If a module is present on face in front of which another casing or a [controller](controller.md) is being placed, the module will automatically be ejected from its casing.

Casings provide four ports for each installed module, which can be used to transfer data across an edge of the casing block. If there is another casing block in the direction of the edge, the data will be transferred through the connecting face and to the adjacent port of the neighboring casing block. Otherwise the port will connect to the module around the respective edge of the casing.

This means there is always a slot for a module behind a port. However, if no module is installed in such a slot, reading from the port leading to it will not succeed, nor will writing to i