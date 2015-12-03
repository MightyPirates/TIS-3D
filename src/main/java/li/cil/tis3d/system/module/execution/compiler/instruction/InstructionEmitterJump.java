package li.cil.tis3d.system.module.execution.compiler.instruction;

import li.cil.tis3d.Constants;
import li.cil.tis3d.system.module.execution.MachineState;
import li.cil.tis3d.system.module.execution.compiler.ParseException;
import li.cil.tis3d.system.module.execution.compiler.Validator;
import li.cil.tis3d.system.module.execution.instruction.Instruction;
import li.cil.tis3d.system.module.execution.instruction.InstructionJump;

import java.util.List;
import java.util.regex.Matcher;

public final class InstructionEmitterJump extends AbstractInstructionEmitter {
    @Override
    public String getInstructionName() {
        return "JMP";
    }

    @Override
    public Instruction compile(final Matcher matcher, final int lineNumber, final List<Validator> validators) throws ParseException {
        final String label = checkArg(lineNumber, matcher, "arg1", "name");
        checkExcess(lineNumber, matcher, "arg2");

        validators.add(state -> validateLabel(state, label, matcher, lineNumber));

        return new InstructionJump(label);
    }

    private static void validateLabel(final MachineState state, final String label, final Matcher matcher, final int lineNumber) throws ParseException {
        if (!state.labels.containsKey(label)) {
            throw new ParseException(Constants.MESSAGE_NO_SUCH_LABEL, lineNumber, matcher.start("arg1"));
        }
    }
}
