package li.cil.tis3d.common.module.execution.target;

import li.cil.tis3d.common.module.execution.Machine;
import li.cil.tis3d.common.module.execution.MachineState;

abstract class AbstractTargetInterface implements TargetInterface {
    private final Machine machine;

    protected AbstractTargetInterface(final Machine machine) {
        this.machine = machine;
    }

    protected Machine getMachine() {
        return machine;
    }

    protected MachineState getState() {
        return machine.getState();
    }
}
