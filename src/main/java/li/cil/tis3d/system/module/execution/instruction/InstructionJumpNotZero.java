package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.MachineState;

public final class InstructionJumpNotZero extends AbstractInstructionJumpConditional {
    public InstructionJumpNotZero(final String label) {
        super(label);
    }

    @Override
    protected boolean isConditionTrue(final MachineState state) {
        return state.acc != 0;
    }

    @Override
    public String toString() {
        return "JNZ " + label;
    }
}
