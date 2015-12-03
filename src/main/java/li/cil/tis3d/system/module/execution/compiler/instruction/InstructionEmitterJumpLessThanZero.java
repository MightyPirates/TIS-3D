package li.cil.tis3d.system.module.execution.compiler.instruction;

import li.cil.tis3d.system.module.execution.compiler.ParseException;
import li.cil.tis3d.system.module.execution.compiler.Validator;
import li.cil.tis3d.system.module.execution.instruction.Instruction;
import li.cil.tis3d.system.module.execution.instruction.InstructionJumpLessThanZero;

import java.util.List;
import java.util.regex.Matcher;

public final class InstructionEmitterJumpLessThanZero extends AbstractInstructionEmitter {
    @Override
    public String getInstructionName() {
        return "JLZ";
    }

    @Override
    public Instruction compile(final Matcher matcher, final int lineNumber, final List<Validator> validators) throws ParseException {
        final String label = checkArg(lineNumber, matcher, "arg1", "name");
        checkExcess(lineNumber, matcher, "arg2");

        return new InstructionJumpLessThanZero(label);
    }
}
