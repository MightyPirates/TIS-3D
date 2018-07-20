package li.cil.tis3d.common.module.execution.compiler.instruction;

import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.module.execution.compiler.Validator;
import li.cil.tis3d.common.module.execution.instruction.Instruction;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Implemented for each individual supported instruction.
 */
public interface InstructionEmitter {
    /**
     * The name of the instruction as used in code, e.g. <tt>MOV</tt>.
     *
     * @return the name of the instruction.
     */
    String getInstructionName();

    /**
     * Compile an instruction.
     *
     * @param matcher    the matcher used to parse the line.
     * @param lineNumber the line number the instruction is on (for exceptions).
     * @param defines    the map of currently active defines.
     * @param validators list of validators instruction emitters may add to.
     * @return the compiled instruction.
     * @throws ParseException if there was a syntax error.
     */
    Instruction compile(final Matcher matcher, final int lineNumber, final Map<String, String> defines, final List<Validator> validators) throws ParseException;
}
