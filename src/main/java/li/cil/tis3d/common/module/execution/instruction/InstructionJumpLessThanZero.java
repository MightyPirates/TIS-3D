package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.MachineState;

public final class InstructionJumpLessThanZero extends AbstractInstructionJumpConditional {
    public InstructionJumpLessThanZero(final String label) {
        super(label);
    }

    @Override
    protected boolean isConditionTrue(final MachineState state) {
        return state.acc < 0;
    }

    @Override
    public String toString() {
        return "JLZ " + label;
    }
}
