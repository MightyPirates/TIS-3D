package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.MachineState;

public final class InstructionSave implements Instruction {
    public static final String NAME = "SAV";
    public static final Instruction INSTANCE = new InstructionSave();

    @Override
    public void step(final Machine machine) {
        final MachineState state = machine.getState();
        state.bak = state.acc;
        state.pc++;
    }

    @Override
    public String toString() {
        return NAME;
    }
}
