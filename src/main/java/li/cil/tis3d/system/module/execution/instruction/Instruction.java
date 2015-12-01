package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.api.Side;
import li.cil.tis3d.system.module.execution.Machine;

public interface Instruction {
    void step(Machine machine);

    void onWriteCompleted(final Machine machine, Side side);
}
