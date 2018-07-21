package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.target.Target;
import li.cil.tis3d.common.module.execution.target.TargetInterface;

public final class InstructionMoveImmediate extends AbstractInstructionMove {
    private final short value;

    public InstructionMoveImmediate(final short value, final Target destination) {
        super(destination);
        this.value = value;
    }

    @Override
    protected void doStep(final Machine machine) {
        final TargetInterface destinationInterface = machine.getInterface(destination);

        if (!destinationInterface.isWriting()) {
            if (destinationInterface.beginWrite(value)) {
                machine.getState().pc++;
            }
        }
    }

    @Override
    public String toString() {
        return InstructionMove.NAME + " " + value + " " + destination;
    }
}
