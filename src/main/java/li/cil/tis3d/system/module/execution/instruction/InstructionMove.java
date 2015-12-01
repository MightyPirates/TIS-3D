package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.api.Side;
import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.MachineState;
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
        final MachineState state = machine.getState();

        if (!machine.isWriting(destination)) {
            if (!machine.isReading(source)) {
                machine.beginRead(source);
            }
            if (machine.isInputTransferring(source)) {
                machine.beginWrite(destination, machine.read(source));
            }
        }
        if (machine.isOutputTransferring(destination)) {
            state.pc++;
        }
    }

    @Override
    public void onWriteCompleted(final Machine machine, final Side side) {
        machine.cancelWrite(destination);
        machine.getState().pc++;
    }
}
