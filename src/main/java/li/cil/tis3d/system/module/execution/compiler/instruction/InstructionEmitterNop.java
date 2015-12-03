package li.cil.tis3d.system.module.execution.compiler.instruction;

import li.cil.tis3d.system.module.execution.compiler.ParseException;
import li.cil.tis3d.system.module.execution.compiler.Validator;
import li.cil.tis3d.system.module.execution.instruction.Instruction;
import li.cil.tis3d.system.module.execution.instruction.InstructionAddImmediate;

import java.util.List;
import java.util.regex.Matcher;

public final class InstructionEmitterNop extends AbstractInstructionEmitter {
    @Override
    public String getInstructionName() {
        return "NOP";
    }

    @Override
    public Instruction compile(final Matcher matcher, final int lineNumber, final List<Validator> validators) throws ParseException {
        checkExcess(lineNumber, matcher, "arg1");
        return new InstructionAddImmediate(0);
    }
}
