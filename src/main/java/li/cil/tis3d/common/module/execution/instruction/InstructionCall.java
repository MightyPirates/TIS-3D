package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.MachineState;

public final class InstructionCall implements Instruction {
    private final String label;

    public InstructionCall(final String label) {
        this.label = label;
    }

    @Override
    public void step(final Machine machine) {
        final MachineState state = machine.getState();
        state.reta = state.pc + 1;
        state.pc = state.labels.get(label);
    }

    @Override
    public String toString() {
        return "CALL " + label;
    }
}