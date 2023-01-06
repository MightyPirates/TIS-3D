package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.MachineState;

public final class LastRotateLeftInstruction implements Instruction {
    public static final String NAME = "RLLAST";
    public static final LastRotateLeftInstruction INSTANCE = new LastRotateLeftInstruction();

    @Override
    public void step(final Machine machine) {
        final MachineState state = machine.getState();
        state.last = state.last.map(p -> p.rotated(-1));
        state.pc++;
    }

    @Override
    public String toString() {
        return NAME;
    }
}
