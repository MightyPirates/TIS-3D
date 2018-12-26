package li.cil.tis3d.common.module.execution.compiler.instruction;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.module.execution.compiler.Validator;
import li.cil.tis3d.common.module.execution.instruction.Instruction;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public final class MissingInstructionEmitter implements InstructionEmitter {
    @Override
    public Instruction compile(final Matcher matcher, final int lineNumber, final Map<String, String> defines, final List<Validator> validators) throws ParseException {
        throw new ParseException(Constants.MESSAGE_INVALID_INSTRUCTION, lineNumber, matcher.start("name"), matcher.end("name"));
    }
}
