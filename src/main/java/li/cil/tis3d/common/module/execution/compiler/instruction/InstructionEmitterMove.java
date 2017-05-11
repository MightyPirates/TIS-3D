package li.cil.tis3d.common.module.execution.compiler.instruction;

import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.module.execution.compiler.Validator;
import li.cil.tis3d.common.module.execution.instruction.Instruction;
import li.cil.tis3d.common.module.execution.instruction.InstructionMove;
import li.cil.tis3d.common.module.execution.instruction.InstructionMoveImmediate;
import li.cil.tis3d.common.module.execution.target.Target;

import java.util.List;
import java.util.regex.Matcher;

public final class InstructionEmitterMove extends AbstractInstructionEmitter {
    @Override
    public String getInstructionName() {
        return "MOV";
    }

    @Override
    public Instruction compile(final Matcher matcher, final int lineNumber, final List<Validator> validators) throws ParseException {
        final Object src = checkTargetOrNumber(lineNumber,
                                               checkArg(lineNumber, matcher, "arg1", "name"),
                                               matcher.start("arg1"), matcher.end("arg1"));
        final Target dst = checkTarget(lineNumber,
                                       checkArg(lineNumber, matcher, "arg2", "arg1"),
                                       matcher.start("arg2"), matcher.end("arg2"));
        checkExcess(lineNumber, matcher, "excess");

        if (src instanceof Target) {
            return new InstructionMove((Target) src, dst);
        } else /* if (src instanceof Integer) */ {
            return new InstructionMoveImmediate((Short) src, dst);
        }
    }
}
