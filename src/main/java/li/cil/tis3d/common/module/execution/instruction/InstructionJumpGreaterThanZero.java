package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.MachineState;

public final class InstructionJumpGreaterThanZero extends AbstractInstructionJumpConditional {
    public InstructionJumpGreaterThanZero(final String label) {
        super(label);
    }

    @Override
    protected boolean isConditionTrue(final MachineState state) {
        return state.acc > 0;
    }

    @Override
    public String toString() {
        return "JGZ " + label;
    }
}
