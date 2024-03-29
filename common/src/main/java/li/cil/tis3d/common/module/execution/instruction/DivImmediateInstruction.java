package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.api.machine.HaltAndCatchFireException;
import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.MachineState;

public final class DivImmediateInstruction implements Instruction {
    private final short value;

    public DivImmediateInstruction(final short value) {
        this.value = value;
    }

    @Override
    public void step(final Machine machine) {
        if (value == 0) {
            throw new HaltAndCatchFireException();
        }

        final MachineState state = machine.getState();
        state.acc = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, state.acc / value));
        state.pc++;
    }

    @Override
    public String toString() {
        return DivInstruction.NAME + " " + value;
    }
}
