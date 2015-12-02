package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.target.Target;

public final class InstructionJumpRelative extends AbstractInstruction {
    private final Target source;

    public InstructionJumpRelative(final Target source) {
        this.source = source;
    }

    @Override
    public void step(final Machine machine) {
        if (!machine.isReading(source)) {
            machine.beginRead(source);
        }
        if (machine.canRead(source)) {
            machine.getState().pc += machine.read(source);
        }
    }
}
