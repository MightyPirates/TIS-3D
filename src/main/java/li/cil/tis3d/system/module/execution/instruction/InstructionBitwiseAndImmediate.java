package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.MachineState;

public final class InstructionBitwiseAndImmediate implements Instruction {
    private final int value;

    public InstructionBitwiseAndImmediate(final int value) {
        this.value = value;
    }

    @Override
    public void step(final Machine machine) {
        final MachineState state = machine.getState();
        state.acc &= value;
        state.pc++;
    }

    @Override
    public String toString() {
        return "AND " + value;
    }
}
