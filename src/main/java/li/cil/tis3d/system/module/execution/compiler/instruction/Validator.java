package li.cil.tis3d.system.module.execution.compiler.instruction;

import li.cil.tis3d.system.module.execution.MachineState;
import li.cil.tis3d.system.module.execution.compiler.ParseException;

/**
 * Type of validators instruction emitters may register to be run as a post-processing step.
 */
@FunctionalInterface
public interface Validator {
    void accept(MachineState state) throws ParseException;
}
