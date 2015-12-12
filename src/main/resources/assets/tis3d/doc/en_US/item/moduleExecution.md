# Execution Module

![Execuuuute!](item:tis3d:moduleExecution)

The execution module is the primary means of programming a TIS-3D computer. When installed in a [casing](../block/casing.md), it can be programmed by using a book on it, containing the code to program it with. For more a more convenient development experience, consider investing in a [Code Bible](bookCode.md), the true and tested tool of any professional.

## Architecture
The execution module allows the user to control the operation of a TIS-3D computer with great flexibility. Each execution module can be programmed using a primitive assembly language, providing an optimized set of instructions. While active, the execution module processes the programmed instructions one by one, starting with the first instruction in the program. All instructions that are not jump instructions advance the program counter to the next instruction in the program after finishing their operation. If the program counter leaves the valid range of instructions, the program automatically continues from the first instruction. *This directly implies that programs loop* unless explicitly halted.

Instructions may operate on targets of different kinds. A target is a valid source for reading a value from, or sink for writing a value to. Valid targets include the four ports of the execution module, the execution module's registers and a small number of virtual registers and ports.

A TIS-3D computer, and in extension the execution module, supports a value range of 16 Bit, treated as a single, signed value ranging from -32768 to 32767. *Due to technical limitations, some modules, including the execution module, may choose to display values as unsigned hexadecimal representations*.

## Targets
`ACC`
Register. This is the primary register of an execution module. Arithmetic operations will usually store their result in this register.

`BAK`
Non-addressable register. Special register that can be used to store a value from `ACC`. It cannot be addressed directly, but must instead be accessed using the `SAV` and `SWP` instructions.

`NIL`
Virtual register. This is a pseudo-target that may be written to to dispose values, or read from to produce zero values.

`LEFT`, `RIGHT`, `UP`, `DOWN`
Port. These represent the four ports of the execution module. Operation on the external ports is generally slower than operating on internal registers.

`ANY`
Virtual port. This is a pseudo-target that will perform an operation on all ports simultaneously, but will only actually perform an operation on the first port to finish the operation. For example, `MOV 10 ANY` will begin writing the value 10 to all ports, but as soon as it was read from one port, it can no longer be read from all other ports; the write operation on all ports will effectively be canceled after one succeeded.

`LAST`
Virtual port. This is a pseudo-target that will store the *actual* port that finished the last operation that used the `ANY` pseudo-target.

## Language Specification
In addition to a list of instructions, assembler code provided to an execution module may contain metadata. Comments are textual notes in the code that are completely ignored in the execution of the program. Labels mark positions in the code that can be addressed by jump instructions. Comments, labels and blank lines have no influence on the addressing of the compiled program. This is relevant when using the `JRO` instruction.

### Comments
Comments are denoted by a leading `#` (#) hash character. They may either appear as the sole content of a line, or on the same line as an instruction or label.
Example:
`# Single line comment`
`LOOP: # Start of loop`
`MOV 0, ACC # Reset`

### Labels
Labels are denoted by a string followed by a `:` (:) color character. A label always refers to the instruction following it. When used as the target of a jump instruction, that instruction is be jumped to directly. This also means that with respect to the program's execution, it makes no difference whether a label is placed on the same line, or the line preceeding the instruction it references.
Example:
`START: MOV 8, ACC`
`LOOP:`
`SUB 1`
`JGZ LOOP`
`JMP START`
`# Never reached`

### Instructions
`NOP`
Pseudo-instruction that has no effect on the state of the execution module's ports nor registers, i.e. no influence on state. `NOP` is automatically compiled to `ADD NIL`.

#### Data Transfer
`MOV <SRC> <DST>`
Transfers data from the target `SRC` to the target `DST`. See above for a list of valid targets. Note that operating on registers / internal state is typically faster than operating on ports. Exact timings are not part of the specification, and vendor specific.
Exaple:
`MOV 8, ACC` Writes the value 8 to the `ACC` register.
`MOV LEFT, RIGHT` Reads a value from the left port, then writes the read value to the right port.
`MOV DOWN, NIL` Reads the value from the bottom port and writes it to `NIL`, effectively discarding it.

`SWP`
Exchanges the current values of the `ACC` and `BAK` registers.

`SAV`
Copies the current value of `ACC` to `BAK`.

#### Arithmetic Operations
`ADD <SRC>`
Reads a value from the specified target `SRC` and adds it to the current value of `ACC`, then writes the result of the operation back to `ACC`. Note that arithmetic over- or underflows are clamped, i.e. there is no over- or underflow. The value will merely cap out at the end of the range of valid values.
Example:
`ADD 1` Adds one to the current value of `ACC`.
`ADD LEFT` Reads a value from the left port, then adds it to `ACC`.

`SUB <SRC>`
Reads a value from the specified target `SRC` and subtracts it from the current value of `ACC`, then writes the result of the operation back to `ACC`. Note that arithmetic over- or underflows are clamped, i.e. there is no over- or underflow. The value will merely cap out at the end of the range of valid values.
Example:
`SUB 1` Subtracts one to the current value of `ACC`.
`SUB LEFT` Reads a value from the left port, then subtracts it from `ACC`.

`NEG`
Negates the current value of `ACC` and stores the result in `ACC`.

### Bitwise Operations
TODO

### Control Flow
`JMP <LABEL>`
Unconditionally jump to the instruction referenced by the specified label `LABEL`.

`JEZ <LABEL>`
If the current value of `ACC` is zero (0), jump to the instruction referenced by the specified label `LABEL`. Otherwise continue with the next operation in the program.

`JNZ <LABEL>`
If the current value of `ACC` is *not* zero (0), jump to the instruction referenced by the specified label `LABEL`. Otherwise continue with the next operation in the program.

`JGZ <LABEL>`
If the current value of `ACC` is *greater than* zero (0), jump to the instruction referenced by the specified label `LABEL`. Otherwise continue with the next operation in the program.

`JLZ <LABEL>`
If the current value of `ACC` is *less than* zero (0), jump to the instruction referenced by the specified label `LABEL`. Otherwise continue with the next operation in the program.

`JRO <SRC>`
Unconditionally jump to a relative address, read from the specified target `SRC`. This modifies the program counter by adding the value read from `SRC` to it. Execution resumes at the new address. `JRO 0` effectively halts the execution module indefinitely.
