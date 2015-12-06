package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.MachineState;
import li.cil.tis3d.system.module.execution.target.Target;
import li.cil.tis3d.system.module.execution.target.TargetInterface;

public final class InstructionBitwiseAnd implements Instruction {
    private final Target source;

    public InstructionBitwiseAnd(final Target source) {
        this.source = source;
    }

    @Override
    public void step(final Machine machine) {
        final TargetInterface sourceInterface = machine.getInterface(source);

        if (!sourceInterface.isReading()) {
            sourceInterface.beginRead();
        }
        if (sourceInterface.canTransfer()) {
            final MachineState state = machine.getState();
            state.acc &= sourceInterface.read();
            state.pc++;
        }
    }

    @Override
    public String toString() {
        return "AND " + source;
    }
}
