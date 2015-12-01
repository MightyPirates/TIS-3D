package li.cil.tis3d.system.module.execution.compiler.instruction;

import li.cil.tis3d.system.module.execution.compiler.ParseException;
import li.cil.tis3d.system.module.execution.instruction.Instruction;

import java.util.List;
import java.util.regex.Matcher;

public final class InstructionEmitterMissing implements InstructionEmitter {
    private static final String MESSAGE_UNKNOWN_INSTRUCTION = "Unknown instruction";

    @Override
    public String getInstructionName() {
        return null;
    }

    @Override
    public Instruction compile(final Matcher matcher, final int lineNumber, final List<Validator> validators) throws ParseException {
        throw new ParseException(MESSAGE_UNKNOWN_INSTRUCTION, lineNumber, matcher.start("name"));
    }
}
