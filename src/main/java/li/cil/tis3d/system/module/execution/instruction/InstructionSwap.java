package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.MachineState;

public final class InstructionSwap implements Instruction {
    public static final Instruction INSTANCE = new InstructionSwap();

    @Override
    public void step(final Machine machine) {
        final MachineState state = machine.getState();
        final int tmp = state.acc;
        state.acc = state.bak;
        state.bak = tmp;
        state.pc++;
    }

    @Override
    public String toString() {
        return "SWP";
    }
}
