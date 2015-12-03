package li.cil.tis3d.system.module.execution.compiler.instruction;

import li.cil.tis3d.system.module.execution.compiler.ParseException;
import li.cil.tis3d.system.module.execution.compiler.Validator;
import li.cil.tis3d.system.module.execution.instruction.Instruction;
import li.cil.tis3d.system.module.execution.instruction.InstructionSubtract;
import li.cil.tis3d.system.module.execution.instruction.InstructionSubtractImmediate;
import li.cil.tis3d.system.module.execution.target.Target;

import java.util.List;
import java.util.regex.Matcher;

public final class InstructionEmitterSubtract extends AbstractInstructionEmitter {
    @Override
    public String getInstructionName() {
        return "SUB";
    }

    @Override
    public Instruction compile(final Matcher matcher, final int lineNumber, final List<Validator> validators) throws ParseException {
        final Object src = checkTargetOrInt(lineNumber,
                checkArg(lineNumber, matcher, "arg1", "name"),
                matcher.start("arg1"));
        checkExcess(lineNumber, matcher, "arg2");

        if (src instanceof Target) {
            return new InstructionSubtract((Target) src);
        } else /* if (src instanceof Integer) */ {
            return new InstructionSubtractImmediate((Integer) src);
        }
    }
}
