package li.cil.tis3d.common.module.execution.compiler;

import li.cil.tis3d.common.module.execution.MachineState;

/**
 * Type of validators instruction emitters may register to be run as a post-processing step.
 */
@FunctionalInterface
public interface Validator {
    /**
     * Called from the {@link Compiler} after all lines have been parsed and
     * all instructions have been generated to allow validation of the
     * generated machine state.
     *
     * @param state the machine state to validate.
     * @throws ParseException if the generated state is invalid in some way.
     */
    void accept(final MachineState state) throws ParseException;
}
