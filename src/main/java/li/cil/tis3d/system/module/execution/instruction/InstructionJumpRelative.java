package li.cil.tis3d.system.module.execution.instruction;

import li.cil.tis3d.system.module.execution.Machine;
import li.cil.tis3d.system.module.execution.MachineState;
import li.cil.tis3d.system.module.execution.target.Target;

public final class InstructionJumpRelative extends AbstractInstruction {
    private final Target source;

    public InstructionJumpRelative(final Target source) {
        this.source = source;
    }

    @Override
    public void step(final Machine machine) {
        final MachineState state = machine.getState();
        if (!machine.isReading(source)) {
            machine.beginRead(source);
        }
        if (machine.isInputTransferring(source)) {
            state.pc += machine.read(source);
        }
    }
}
