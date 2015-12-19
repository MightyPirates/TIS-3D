package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;

public final class InstructionJumpRelativeImmediate implements Instruction {
    private final short delta;

    public InstructionJumpRelativeImmediate(final short delta) {
        this.delta = delta;
    }

    @Override
    public void step(final Machine machine) {
        machine.getState().pc += delta;
    }

    @Override
    public String toString() {
        return "JRO " + delta;
    }
}
