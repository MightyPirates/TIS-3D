package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.MachineState;

public final class InstructionMulImmediate implements Instruction {
    private final short value;

    public InstructionMulImmediate(final short value) {
        this.value = value;
    }

    @Override
    public void step(final Machine machine) {
        final MachineState state = machine.getState();
        state.acc = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, state.acc * value));
        state.pc++;
    }

    @Override
    public String toString() {
        return InstructionMul.NAME + " " + value;
    }
}
