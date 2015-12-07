package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.MachineState;
import li.cil.tis3d.system.module.execution.target.Target;
import li.cil.tis3d.system.module.execution.target.TargetInterface;

public final class InstructionBitwiseNot implements Instruction {
    @Override
    public void step(final Machine machine) {
        final MachineState state = machine.getState();
        state.acc = ~state.acc;
        state.pc++;
    }

    @Override
    public String toString() {
        return "NOT";
    }
}
