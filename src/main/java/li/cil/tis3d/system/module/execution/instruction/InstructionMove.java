package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.target.Target;

public final class InstructionMove extends AbstractInstructionMove {
    private final Target source;

    public InstructionMove(final Target source, final Target destination) {
        super(destination);
        this.source = source;
    }

    @Override
    protected void doStep(final Machine machine) {
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
}
