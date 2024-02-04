package li.cil.tis3d.common.module.execution.instruction;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.target.Target;
import li.cil.tis3d.common.module.execution.target.TargetInterface;

public class MoveLabelInstruction extends AbstractMoveInstruction {
    private final String label;

    public MoveLabelInstruction(final String label, final Target destination) {
        super(destination);
        this.label = label;
    }

    @Override
    public void step(final Machine machine) {
        final TargetInterface destinationInterface = machine.getInterface(destination);
        int addr = machine.getState().labels.get(label);

        if (!destinationInterface.isWriting()) {
            if (destinationInterface.beginWrite((short) addr)) {
                machine.getState().pc++;
            }
        }
    }

    @Override
    public String toString() {
        return MoveInstruction.NAME + " " + label + " " + destination;
    }
}
