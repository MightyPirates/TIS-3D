package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.api.Port;
import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.target.Target;

public final class InstructionMoveImmediate extends AbstractInstruction {
    private final int value;
    private final Target destination;

    public InstructionMoveImmediate(final int value, final Target destination) {
        this.value = value;
        this.destination = destination;
    }

    @Override
    public void step(final Machine machine) {
        if (!machine.isWriting(destination)) {
            if (machine.beginWrite(destination, value)) {
                machine.getState().pc++;
            }
        }
    }

    @Override
    public void onWriteCompleted(final Machine machine, final Port port) {
        if (destination == Target.ANY) {
            machine.cancelWrite(destination);
        }
        machine.getState().pc++;
    }
}
