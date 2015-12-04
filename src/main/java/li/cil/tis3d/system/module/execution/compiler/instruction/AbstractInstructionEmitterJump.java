package li.cil.tis3d.system.module.execution.compiler.instruction;

import li.cil.tis3d.Constants;
import li.cil.tis3d.system.module.execution.MachineState;
import li.cil.tis3d.system.module.execution.compiler.ParseException;

import java.util.regex.Matcher;

abstract class AbstractInstructionEmitterJump extends AbstractInstructionEmitter {
    protected static void validateLabel(final MachineState state, final String label, final Matcher matcher, final int lineNumber) throws ParseException {
        if (!state.labels.containsKey(label)) {
            throw new ParseException(Constants.MESSAGE_NO_SUCH_LABEL, lineNumber, matcher.start("arg1"));
        }
    }
}
