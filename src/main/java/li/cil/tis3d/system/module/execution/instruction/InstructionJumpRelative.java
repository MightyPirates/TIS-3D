package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.target.Target;
import li.cil.tis3d.system.module.execution.target.TargetInterface;

public final class InstructionJumpRelative extends AbstractInstruction {
    private final Target source;

    public InstructionJumpRelative(final Target source) {
        this.source = source;
    }

    @Override
    public void step(final Machine machine) {
        final TargetInterface sourceInterface = machine.getInterface(source);

        if (!sourceInterface.isReading()) {
            sourceInterface.beginRead();
        }
        if (sourceInterface.canRead()) {
            machine.getState().pc += sourceInterface.read();
        }
    }
}
