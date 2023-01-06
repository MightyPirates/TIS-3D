package li.cil.tis3d.common.module.execution;

import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.common.module.ExecutionModule;
import li.cil.tis3d.common.module.execution.target.Target;
import li.cil.tis3d.common.module.execution.target.TargetInterface;

/**
 * Interface for the virtual machine used by the {@link ExecutionModule}.
 * <p>
 * Provided to {@link li.cil.tis3d.common.module.execution.instruction.Instruction}s on top of the
 * state itself for unified data transfer to all valid targets, including virtual ones such as
 * {@link Target#ANY}.
 */
public interface Machine {
    /**
     * Get the state of the machine, i.e. registers and program.
     *
     * @return the state of the machine.
     */
    MachineState getState();

    /**
     * Get an interface that allows reading and writing to all valid
     * {@link Target}s, including virtual ones. A {@link TargetInterface} is
     * basically a {@link Pipe} wrapper and register wrapper
     * in one.
     *
     * @param target the target to get the interface for.
     * @return the interface for the specified target.
     */
    TargetInterface getInterface(final Target target);
}
