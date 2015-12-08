package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.target.Target;
import li.cil.tis3d.common.module.execution.target.TargetInterface;

public final class InstructionJumpRelative implements Instruction {
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
        if (sourceInterface.canTransfer()) {
            machine.getState().pc += sourceInterface.read();
        }
    }

    @Override
    public String toString() {
        return "JRO " + source;
    }
}
