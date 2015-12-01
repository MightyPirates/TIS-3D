package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.api.Side;
import li.cil.tis3d.system.module.execution.Machine;

abstract class AbstractInstruction implements Instruction {
    @Override
    public void onWriteCompleted(final Machine machine, final Side side) {
    }
}
