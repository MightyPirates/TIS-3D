package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.target.Target;
import li.cil.tis3d.common.module.execution.target.TargetInterface;

abstract class AbstractReadInstruction implements Instruction {
    protected final Target source;

    protected AbstractReadInstruction(final Target source) {
        this.source = source;
    }

    @Override
    public final void step(final Machine machine) {
        final TargetInterface sourceInterface = machine.getInterface(source);

        if (!sourceInterface.isReading()) {
            sourceInterface.beginRead();
        }
        if (sourceInterface.canTransfer()) {
            doStep(machine, sourceInterface.read());
        }
    }

    protected abstract void doStep(final Machine machine, final int value);
}
