package li.cil.tis3d.common.module.execution.compiler.instruction;

import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.module.execution.compiler.Validator;
import li.cil.tis3d.common.module.execution.instruction.Instruction;
import li.cil.tis3d.common.module.execution.target.Target;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;

public final class TargetOrImmediateInstructionEmitter extends AbstractInstructionEmitter {
    private final Function<Target, Instruction> constructorTarget;
    private final Function<Short, Instruction> constructorImmediate;

    public TargetOrImmediateInstructionEmitter(final Function<Target, Instruction> target, final Function<Short, Instruction> immediate) {
        this.constructorTarget = target;
        this.constructorImmediate = immediate;
    }

    @Override
    public Instruction compile(final Matcher matcher, final int lineNumber, final Map<String, String> defines, final List<Validator> validators) throws ParseException {
        final Object src = checkTargetOrNumber(checkArg(lineNumber, matcher, "arg1", "name"),
            lineNumber, defines, matcher.start("arg1"), matcher.end("arg1"));
        checkExcess(lineNumber, matcher, "arg2");

        if (src instanceof Target) {
            return constructorTarget.apply((Target) src);
        } else /* if (src instanceof Integer) */ {
            return constructorImmediate.apply((Short) src);
        }
    }
}
