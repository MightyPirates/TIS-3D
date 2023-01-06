package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.api.machine.HaltAndCatchFireException;
import li.cil.tis3d.common.module.execution.Machine;

public final class HaltAndCatchFireInstruction implements Instruction {
    public static final String NAME = "HCF";
    public static final Instruction INSTANCE = new HaltAndCatchFireInstruction();

    @Override
    public void step(final Machine machine) {
        throw new HaltAndCatchFireException();
    }

    @Override
    public String toString() {
        return NAME;
    }
}
