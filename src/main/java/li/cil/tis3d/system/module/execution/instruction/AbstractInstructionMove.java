package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.api.Port;
import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.target.Target;

abstract class AbstractInstructionMove implements Instruction {
    protected final Target destination;

    // Flag for finishing in step if we get an onWriteComplete before it.
    // Note that this can only ever happen if step is also called in the
    // same update cycle, so it is not necessary to persist this flag.
    private boolean isDone;

    protected AbstractInstructionMove(final Target destination) {
        this.destination = destination;
    }

    @Override
    public void step(final Machine machine) {
        if (isDone) {
            isDone = false;
            machine.getState().pc++;
        } else {
            doStep(machine);
        }
    }

    protected abstract void doStep(final Machine machine);

    @Override
    public void onWriteCompleted(final Machine machine, final Port port) {
        machine.getInterface(destination).onWriteComplete();
        isDone = true;
    }
}
