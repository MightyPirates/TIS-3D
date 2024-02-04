package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.target.Target;
import li.cil.tis3d.common.module.execution.target.TargetInterface;

public class JumpAbsoluteInstruction implements Instruction {
    public static final String NAME = "JAB";

    private final Target source;

    public JumpAbsoluteInstruction(final Target source) {
        this.source = source;
    }

    @Override
    public void step(final Machine machine) {
        final TargetInterface sourceInterface = machine.getInterface(source);

        if (!sourceInterface.isReading()) {
            sourceInterface.beginRead();
        }
        if (sourceInterface.canTransfer()) {
            machine.getState().pc = sourceInterface.read();
        }
    }

    @Override
    public String toString() {
        return NAME + " " + source;
    }
}
