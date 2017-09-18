package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.api.machine.HaltAndCatchFireException;
import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.MachineState;
import li.cil.tis3d.common.module.execution.target.Target;

public final class InstructionDiv extends AbstractInstructionRead {
    public InstructionDiv(final Target source) {
        super(source);
    }

    @Override
    protected void doStep(final Machine machine, final int value) {
        if (value == 0) {
            throw new HaltAndCatchFireException();
        }

        final MachineState state = machine.getState();
        state.acc = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, state.acc / value));
        state.pc++;
    }

    @Override
    public String toString() {
        return "DIV " + source;
    }
}
