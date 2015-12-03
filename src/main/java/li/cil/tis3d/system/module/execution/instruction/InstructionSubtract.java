package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.MachineState;
import li.cil.tis3d.system.module.execution.target.Target;
import li.cil.tis3d.system.module.execution.target.TargetInterface;

public final class InstructionSubtract extends AbstractInstruction {
    private final Target source;

    public InstructionSubtract(final Target source) {
        this.source = source;
    }

    @Override
    public void step(final Machine machine) {
        final TargetInterface sourceInterface = machine.getInterface(source);

        if (!sourceInterface.isReading()) {
            sourceInterface.beginRead();
        }
        if (sourceInterface.canRead()) {
            final MachineState state = machine.getState();
            state.acc -= sourceInterface.read();
            state.pc++;
        }
    }
}
