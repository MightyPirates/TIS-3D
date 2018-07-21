package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.MachineState;

public final class InstructionLastRotateRight implements Instruction {
    public static final String NAME = "RRLAST";

    @Override
    public void step(Machine machine) {
        final MachineState state = machine.getState();
        state.last = state.last.map(p -> p.rotated(1));
        state.pc++;
    }

    @Override
    public String toString() {
        return NAME;
    }
}
