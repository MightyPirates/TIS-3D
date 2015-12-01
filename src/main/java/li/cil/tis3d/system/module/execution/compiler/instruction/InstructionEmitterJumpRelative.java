package li.cil.tis3d.system.module.execution.compiler.instruction;

import li.cil.tis3d.system.module.execution.compiler.ParseException;
import li.cil.tis3d.system.module.execution.instruction.Instruction;
import li.cil.tis3d.system.module.execution.instruction.InstructionJumpRelative;
import li.cil.tis3d.system.module.execution.instruction.InstructionJumpRelativeImmediate;
import li.cil.tis3d.system.module.execution.target.Target;

import java.util.List;
import java.util.regex.Matcher;

public final class InstructionEmitterJumpRelative extends AbstractInstructionEmitter {
    @Override
    public String getInstructionName() {
        return "JRO";
    }

    @Override
    public Instruction compile(final Matcher matcher, final int lineNumber, final List<Validator> validators) throws ParseException {
        final Object src = checkTargetOrInt(lineNumber,
                checkArg(lineNumber, matcher, "arg1", "name"),
                matcher.start("arg1"));
        checkExcess(lineNumber, matcher, "arg2");

        if (src instanceof Target) {
            return new InstructionJumpRelative((Target) src);
        } else {
            return new InstructionJumpRelativeImmediate((Integer) src);
        }
    }
}
