package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.MachineState;
import li.cil.tis3d.common.module.execution.target.Target;

public final class BitwiseShiftLeftInstruction extends AbstractInstructionRead {
    public static final String NAME = "SHL";

    public BitwiseShiftLeftInstruction(final Target source) {
        super(source);
    }

    @Override
    protected void doStep(final Machine machine, final int value) {
        final MachineState state = machine.getState();
        state.acc <<= value;
        state.pc++;
    }

    @Override
    public String toString() {
        return NAME + " " + source;
    }
}
