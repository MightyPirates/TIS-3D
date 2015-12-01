package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.api.Side;
import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.MachineState;
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
        final MachineState state = machine.getState();

        if (!machine.isWriting(destination)) {
            machine.beginWrite(destination, value);
        }
        if (machine.isOutputTransferring(destination)) {
            state.pc++;
        }
    }

    @Override
    public void onWriteCompleted(final Machine machine, final Side side) {
        machine.getState().pc++;
    }
}
