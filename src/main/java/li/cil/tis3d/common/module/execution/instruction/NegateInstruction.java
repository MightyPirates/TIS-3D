package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.MachineState;

public final class NegateInstruction implements Instruction {
    public static final String NAME = "NEG";
    public static final Instruction INSTANCE = new NegateInstruction();

    @Override
    public void step(final Machine machine) {
        final MachineState state = machine.getState();
        state.acc = (short) -state.acc;
        state.pc++;
    }

    @Override
    public String toString() {
        return NAME;
    }
}
