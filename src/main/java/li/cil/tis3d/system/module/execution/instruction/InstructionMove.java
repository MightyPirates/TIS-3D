package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.api.Port;
import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.target.Target;

public final class InstructionMove extends AbstractInstruction {
    private final Target source;
    private final Target destination;

    public InstructionMove(final Target source, final Target destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public void step(final Machine machine) {
        if (!machine.isWriting(destination)) {
            if (!machine.isReading(source)) {
                machine.beginRead(source);
            }
            if (machine.canRead(source)) {
                if (machine.beginWrite(destination, machine.read(source))) {
                    machine.getState().pc++;
                }
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
