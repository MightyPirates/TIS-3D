package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.target.Target;
import li.cil.tis3d.common.module.execution.target.TargetInterface;

public final class MoveImmediateInstruction extends AbstractMoveInstruction {
    private final short value;

    public MoveImmediateInstruction(final short value, final Target destination) {
        super(destination);
        this.value = value;
    }

    @Override
    public void step(final Machine machine) {
        final TargetInterface destinationInterface = machine.getInterface(destination);

        if (!destinationInterface.isWriting()) {
            if (destinationInterface.beginWrite(value)) {
                machine.getState().pc++;
            }
        }
    }

    @Override
    public String toString() {
        return MoveInstruction.NAME + " " + value + " " + destination;
    }
}
