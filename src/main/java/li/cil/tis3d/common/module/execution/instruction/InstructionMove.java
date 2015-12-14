package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.target.Target;
import li.cil.tis3d.common.module.execution.target.TargetInterface;

public final class InstructionMove extends AbstractInstructionMove {
    private final Target source;

    public InstructionMove(final Target source, final Target destination) {
        super(destination);
        this.source = source;
    }

    @Override
    protected void doStep(final Machine machine) {
        final TargetInterface sourceInterface = machine.getInterface(source);
        final TargetInterface destinationInterface = machine.getInterface(destination);

        if (!destinationInterface.isWriting()) {
            if (!sourceInterface.isReading()) {
                sourceInterface.beginRead();
            }
            if (sourceInterface.canTransfer()) {
                final int value = sourceInterface.read();
                if (destinationInterface.beginWrite(value)) {
                    machine.getState().pc++;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "MOV " + source + " " + destination;
    }
}
