package li.cil.tis3d.common.module.execution.compiler.instruction;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.module.execution.MachineState;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.module.execution.compiler.Validator;
import li.cil.tis3d.common.module.execution.instruction.Instruction;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;

public final class LabelInstructionEmitter extends AbstractInstructionEmitter {
    private final Function<String, Instruction> constructor;

    public LabelInstructionEmitter(final Function<String, Instruction> constructor) {
        this.constructor = constructor;
    }

    @Override
    public Instruction compile(final Matcher matcher, final int lineNumber, final Map<String, String> defines, final List<Validator> validators) throws ParseException {
        final String label = checkArg(lineNumber, matcher, "arg1", "name");
        checkExcess(lineNumber, matcher, "arg2");

        validators.add(state -> validateLabel(state, label, matcher, lineNumber));

        return constructor.apply(label);
    }

    private static void validateLabel(final MachineState state, final String label, final Matcher matcher, final int lineNumber) throws ParseException {
        if (!state.labels.containsKey(label)) {
            throw new ParseException(Constants.MESSAGE_LABEL_NOT_FOUND, lineNumber, matcher.start("arg1"), matcher.end("arg1"));
        }
    }
}
