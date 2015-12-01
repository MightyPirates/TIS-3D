package li.cil.tis3d.system.module.execution.compiler.instruction;

import li.cil.tis3d.system.module.execution.compiler.ParseException;
import li.cil.tis3d.system.module.execution.instruction.Instruction;
import li.cil.tis3d.system.module.execution.instruction.InstructionNegate;

import java.util.List;
import java.util.regex.Matcher;

public final class InstructionEmitterNeg extends AbstractInstructionEmitter {
    @Override
    public String getInstructionName() {
        return "NEG";
    }

    @Override
    public Instruction compile(final Matcher matcher, final int lineNumber, final List<Validator> validators) throws ParseException {
        checkExcess(lineNumber, matcher, "arg1");

        return new InstructionNegate();
    }
}
