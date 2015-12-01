package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.MachineState;

public final class InstructionSave extends AbstractInstruction {
    @Override
    public void step(final Machine machine) {
        final MachineState state = machine.getState();
        state.bak = state.acc;
        state.pc++;
    }
}
