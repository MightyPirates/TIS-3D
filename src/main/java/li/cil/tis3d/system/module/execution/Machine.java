package li.cil.tis3d.system.module.execution;

import li.cil.tis3d.system.module.execution.target.Target;

/**
 * Interface for the virtual machine used by the {@link li.cil.tis3d.system.module.ModuleExecution}.
 */
public interface Machine {
    MachineState getState();

    void beginWrite(Target target, int value);

    void cancelWrite(Target target);

    boolean isWriting(Target target);

    boolean isOutputTransferring(Target target);

    void beginRead(Target target);

    boolean isReading(Target target);

    boolean isInputTransferring(Target target);

    int read(Target target);
}
