package li.cil.tis3d.common.module.execution.compiler.instruction;

import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.module.execution.compiler.Validator;
import li.cil.tis3d.common.module.execution.instruction.Instruction;

import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;

public final class InstructionEmitterUnary extends AbstractInstructionEmitter {
    private final String name;
    private final Supplier<Instruction> constructor;

    public InstructionEmitterUnary(final String name, final Supplier<Instruction> constructor) {
        this.name = name;
        this.constructor = constructor;
    }

    @Override
    public String getInstructionName() {
        return name;
    }

    @Override
    public Instruction compile(final Matcher matcher, final int lineNumber, final List<Validator> validators) throws ParseException {
        checkExcess(lineNumber, matcher, "arg1");

        return constructor.get();
    }
}
