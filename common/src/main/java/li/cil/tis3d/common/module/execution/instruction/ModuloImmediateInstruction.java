package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.api.machine.HaltAndCatchFireException;
import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.MachineState;

public final class ModuloImmediateInstruction implements Instruction {
    private final short value;

    public ModuloImmediateInstruction(final short value) {
        this.value = value;
    }

    @Override
    public void step(final Machine machine) {
        if (value == 0) {
            throw new HaltAndCatchFireException();
        }

        final MachineState state = machine.getState();
        state.acc %= value;
        state.pc++;
    }

    @Override
    public String toString() {
        return ModuloInstruction.NAME + " " + value;
    }
}
