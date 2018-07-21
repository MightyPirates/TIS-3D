package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.MachineState;

public final class InstructionSwap implements Instruction {
    public static final String NAME = "SWP";
    public static final Instruction INSTANCE = new InstructionSwap();

    @Override
    public void step(final Machine machine) {
        final MachineState state = machine.getState();
        final short tmp = state.acc;
        state.acc = state.bak;
        state.bak = tmp;
        state.pc++;
    }

    @Override
    public String toString() {
        return NAME;
    }
}
