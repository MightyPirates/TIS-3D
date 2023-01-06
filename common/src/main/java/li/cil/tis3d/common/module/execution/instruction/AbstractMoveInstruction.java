package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.MachineImpl;
import li.cil.tis3d.common.module.execution.target.Target;

abstract class AbstractMoveInstruction implements Instruction {
    protected final Target destination;

    protected AbstractMoveInstruction(final Target destination) {
        this.destination = destination;
    }

    @Override
    public void onBeforeWriteComplete(final MachineImpl machine, final Port port) {
        machine.getInterface(destination).onBeforeWriteComplete(port);
    }

    @Override
    public void onWriteCompleted(final Machine machine, final Port port) {
        machine.getState().pc++;
    }
}
