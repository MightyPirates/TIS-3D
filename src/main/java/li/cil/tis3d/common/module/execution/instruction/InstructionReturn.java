package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.MachineState;

public final class InstructionReturn implements Instruction {
    public InstructionReturn() {
        
    }

    @Override
    public void step(final Machine machine) {
        final MachineState state = machine.getState();
        System.out.println(state.reta);
        state.pc = state.reta;
        state.reta = 0;
    }

    @Override
    public String toString() {
        return "RET";
    }
}
