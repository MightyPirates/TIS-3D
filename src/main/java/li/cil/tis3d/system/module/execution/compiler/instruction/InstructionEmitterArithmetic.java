package li.cil.tis3d.system.module.execution.compiler.instruction;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;

import li.cil.tis3d.system.module.execution.compiler.ParseException;
import li.cil.tis3d.system.module.execution.compiler.Validator;
import li.cil.tis3d.system.module.execution.instruction.Instruction;
import li.cil.tis3d.system.module.execution.instruction.InstructionAdd;
import li.cil.tis3d.system.module.execution.instruction.InstructionAddImmediate;
import li.cil.tis3d.system.module.execution.target.Target;

public class InstructionEmitterArithmetic extends AbstractInstructionEmitter {
	private final String name;
	private final Function<Target, Instruction> targetedInstruction;
	private final Function<Integer, Instruction> immediateInstruction;
	
	public InstructionEmitterArithmetic(final String name, final Function<Target, Instruction> target, final Function<Integer, Instruction> immedate) {
		this.name = name;
		this.targetedInstruction = target;
		this.immediateInstruction = immedate;
	}

	@Override
	public String getInstructionName() {
		return name;
	}

	@Override
	public Instruction compile(Matcher matcher, int lineNumber, List<Validator> validators) throws ParseException {
		final Object src = checkTargetOrInt(lineNumber,
                checkArg(lineNumber, matcher, "arg1", "name"),
                matcher.start("arg1"));
        checkExcess(lineNumber, matcher, "arg2");

        if (src instanceof Target) {
            return targetedInstruction.apply((Target) src);
        } else /* if (src instanceof Integer) */ {
            return immediateInstruction.apply((Integer) src);
        }
	}
	
}
