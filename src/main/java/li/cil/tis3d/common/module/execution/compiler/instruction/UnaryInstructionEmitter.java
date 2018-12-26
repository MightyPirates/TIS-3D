package li.cil.tis3d.common.module.execution.compiler.instruction;

import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.module.execution.compiler.Validator;
import li.cil.tis3d.common.module.execution.instruction.Instruction;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;

public final class UnaryInstructionEmitter extends AbstractInstructionEmitter {
    private final Supplier<Instruction> constructor;

    public UnaryInstructionEmitter(final Supplier<Instruction> constructor) {
        this.constructor = constructor;
    }

    @Override
    public Instruction compile(final Matcher matcher, final int lineNumber, final Map<String, String> defines, final List<Validator> validators) throws ParseException {
        checkExcess(lineNumber, matcher, "arg1");

        return constructor.get();
    }
}
