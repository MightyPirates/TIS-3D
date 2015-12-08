package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.MachineState;

public final class InstructionSubtractImmediate implements Instruction {
    private final int value;

    public InstructionSubtractImmediate(final int value) {
        this.value = value;
    }

    @Override
    public void step(final Machine machine) {
        final MachineState state = machine.getState();
        state.acc -= value;
        state.pc++;
    }

    @Override
    public String toString() {
        return "SUB " + value;
    }
}
