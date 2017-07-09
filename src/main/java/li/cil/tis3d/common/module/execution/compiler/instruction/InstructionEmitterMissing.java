package li.cil.tis3d.common.module.execution.compiler.instruction;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.module.execution.compiler.Validator;
import li.cil.tis3d.common.module.execution.instruction.Instruction;

import java.util.List;
import java.util.regex.Matcher;

public final class InstructionEmitterMissing implements InstructionEmitter {
    @Override
    public String getInstructionName() {
        return "";
    }

    @Override
    public Instruction compile(final Matcher matcher, final int lineNumber, final List<Validator> validators) throws ParseException {
        throw new ParseException(Constants.MESSAGE_INVALID_INSTRUCTION, lineNumber, matcher.start("name"), matcher.end("name"));
    }
}
