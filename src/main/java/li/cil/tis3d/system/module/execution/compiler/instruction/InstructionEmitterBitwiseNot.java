package li.cil.tis3d.system.module.execution.compiler.instruction;

import li.cil.tis3d.system.module.execution.compiler.ParseException;
import li.cil.tis3d.system.module.execution.compiler.Validator;
import li.cil.tis3d.system.module.execution.instruction.Instruction;
import li.cil.tis3d.system.module.execution.instruction.InstructionBitwiseNot;

import java.util.List;
import java.util.regex.Matcher;

//TODO: Maybe generify this one like InstructionEmitterArithmetic
public final class InstructionEmitterBitwiseNot extends AbstractInstructionEmitter {
    @Override
    public String getInstructionName() {
        return "NOT";
    }

    @Override
    public Instruction compile(final Matcher matcher, final int lineNumber, final List<Validator> validators) throws ParseException {
        checkExcess(lineNumber, matcher, "arg1");

        return new InstructionBitwiseNot();
    }
}
