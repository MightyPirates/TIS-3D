package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.target.Target;

public final class InstructionMoveImmediate extends AbstractInstructionMove {
    private final int value;

    public InstructionMoveImmediate(final int value, final Target destination) {
        super(destination);
        this.value = value;
    }

    @Override
    protected void doStep(final Machine machine) {
        if (!machine.isWriting(destination)) {
            if (machine.beginWrite(destination, value)) {
                machine.getState().pc++;
            }
        }
    }
}
