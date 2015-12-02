package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.Machine;

public final class InstructionJumpRelativeImmediate extends AbstractInstruction {
    private final int delta;

    public InstructionJumpRelativeImmediate(final int delta) {
        this.delta = delta;
    }

    @Override
    public void step(final Machine machine) {
        machine.getState().pc += delta;
    }
}
