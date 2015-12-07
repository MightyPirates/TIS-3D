package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.MachineState;

public final class InstructionNegate implements Instruction {
    public static final Instruction INSTANCE = new InstructionNegate();

    @Override
    public void step(final Machine machine) {
        final MachineState state = machine.getState();
        state.acc = -state.acc;
        state.pc++;
    }

    @Override
    public String toString() {
        return "NEG";
    }
}
