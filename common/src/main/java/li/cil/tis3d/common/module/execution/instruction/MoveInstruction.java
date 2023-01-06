package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.target.Target;
import li.cil.tis3d.common.module.execution.target.TargetInterface;

public final class MoveInstruction extends AbstractMoveInstruction {
    public static final String NAME = "MOV";

    private final Target source;

    public MoveInstruction(final Target source, final Target destination) {
        super(destination);
        this.source = source;
    }

    @Override
    public void step(final Machine machine) {
        final TargetInterface sourceInterface = machine.getInterface(source);
        final TargetInterface destinationInterface = machine.getInterface(destination);

        if (!destinationInterface.isWriting()) {
            if (!sourceInterface.isReading()) {
                sourceInterface.beginRead();
            }
            if (sourceInterface.canTransfer()) {
                final short value = sourceInterface.read();
                if (destinationInterface.beginWrite(value)) {
                    machine.getState().pc++;
                }
            }
        }
    }

    @Override
    public String toString() {
        return NAME + " " + source + " " + destination;
    }
}
