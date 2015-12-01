package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.MachineState;

public final class InstructionNegate extends AbstractInstruction {
    @Override
    public void step(final Machine machine) {
        final MachineState state = machine.getState();
        state.acc = -state.acc;
        state.pc++;
    }
}
