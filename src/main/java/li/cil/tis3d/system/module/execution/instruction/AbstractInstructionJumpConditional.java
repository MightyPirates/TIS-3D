package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.MachineState;

abstract class AbstractInstructionJumpConditional implements Instruction {
    protected final String label;

    public AbstractInstructionJumpConditional(final String label) {
        this.label = label;
    }

    @Override
    public final void step(final Machine machine) {
        final MachineState state = machine.getState();
        if (isConditionTrue(state)) {
            state.pc = state.labels.get(label);
        } else {
            state.pc++;
        }
    }

    protected abstract boolean isConditionTrue(final MachineState state);
}
