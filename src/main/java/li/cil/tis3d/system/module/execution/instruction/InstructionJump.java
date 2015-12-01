package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.MachineState;

public final class InstructionJump extends AbstractInstruction {
    private final String label;

    public InstructionJump(final String label) {
        this.label = label;
    }

    @Override
    public void step(final Machine machine) {
        final MachineState state = machine.getState();
        state.pc = state.labels.get(label);
    }
}
