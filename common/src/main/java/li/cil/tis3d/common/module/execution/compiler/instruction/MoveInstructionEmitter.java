package li.cil.tis3d.common.module.execution.compiler.instruction;

import li.cil.tis3d.common.module.execution.MachineState;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.module.execution.compiler.Strings;
import li.cil.tis3d.common.module.execution.compiler.Validator;
import li.cil.tis3d.common.module.execution.instruction.Instruction;
import li.cil.tis3d.common.module.execution.instruction.MoveImmediateInstruction;
import li.cil.tis3d.common.module.execution.instruction.MoveInstruction;
import li.cil.tis3d.common.module.execution.instruction.MoveLabelInstruction;
import li.cil.tis3d.common.module.execution.target.Target;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public final class MoveInstructionEmitter extends AbstractInstructionEmitter {
    private static void validateLabel(final MachineState state, final String label, final Matcher matcher, final int lineNumber) throws ParseException {
        if (!state.labels.containsKey(label)) {
            throw new ParseException(Strings.MESSAGE_LABEL_NOT_FOUND, lineNumber, matcher.start("arg1"), matcher.end("arg1"));
        }
    }

    @Override
    public Instruction compile(final Matcher matcher, final int lineNumber, final Map<String, String> defines, final List<Validator> validators) throws ParseException {
        final Object src = checkTargetOrNumberOrLabel(checkArg(lineNumber, matcher, "arg1", "name"),
            lineNumber, defines, matcher.start("arg1"), matcher.end("arg1"));
        final Target dst = checkTarget(checkArg(lineNumber, matcher, "arg2", "arg1"),
            lineNumber, defines, matcher.start("arg2"), matcher.end("arg2"));
        checkExcess(lineNumber, matcher, "excess");

        if (src instanceof final Target target) {
            return new MoveInstruction(target, dst);
        } else if (src instanceof final Short value) {
            return new MoveImmediateInstruction(value, dst);
        } else if (src instanceof String){
            final String label = checkArg(lineNumber, matcher, "arg1", "name");
            validators.add(state -> validateLabel(state, label, matcher, lineNumber));
            return new MoveLabelInstruction(label, dst);
        } else {
            throw new AssertionError();
        }
    }
}
