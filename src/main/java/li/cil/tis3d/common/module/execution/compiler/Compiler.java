package li.cil.tis3d.common.module.execution.compiler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.module.execution.MachineState;
import li.cil.tis3d.common.module.execution.compiler.instruction.InstructionEmitter;
import li.cil.tis3d.common.module.execution.compiler.instruction.InstructionEmitterLabel;
import li.cil.tis3d.common.module.execution.compiler.instruction.InstructionEmitterMissing;
import li.cil.tis3d.common.module.execution.compiler.instruction.InstructionEmitterMove;
import li.cil.tis3d.common.module.execution.compiler.instruction.InstructionEmitterTargetOrImmediate;
import li.cil.tis3d.common.module.execution.compiler.instruction.InstructionEmitterUnary;
import li.cil.tis3d.common.module.execution.instruction.Instruction;
import li.cil.tis3d.common.module.execution.instruction.InstructionAdd;
import li.cil.tis3d.common.module.execution.instruction.InstructionAddImmediate;
import li.cil.tis3d.common.module.execution.instruction.InstructionBitwiseAnd;
import li.cil.tis3d.common.module.execution.instruction.InstructionBitwiseAndImmediate;
import li.cil.tis3d.common.module.execution.instruction.InstructionBitwiseNot;
import li.cil.tis3d.common.module.execution.instruction.InstructionBitwiseOr;
import li.cil.tis3d.common.module.execution.instruction.InstructionBitwiseOrImmediate;
import li.cil.tis3d.common.module.execution.instruction.InstructionBitwiseShiftLeft;
import li.cil.tis3d.common.module.execution.instruction.InstructionBitwiseShiftLeftImmediate;
import li.cil.tis3d.common.module.execution.instruction.InstructionBitwiseShiftRight;
import li.cil.tis3d.common.module.execution.instruction.InstructionBitwiseShiftRightImmediate;
import li.cil.tis3d.common.module.execution.instruction.InstructionBitwiseXor;
import li.cil.tis3d.common.module.execution.instruction.InstructionBitwiseXorImmediate;
import li.cil.tis3d.common.module.execution.instruction.InstructionDiv;
import li.cil.tis3d.common.module.execution.instruction.InstructionDivImmediate;
import li.cil.tis3d.common.module.execution.instruction.InstructionHaltAndCatchFire;
import li.cil.tis3d.common.module.execution.instruction.InstructionJump;
import li.cil.tis3d.common.module.execution.instruction.InstructionJumpEqualZero;
import li.cil.tis3d.common.module.execution.instruction.InstructionJumpGreaterThanZero;
import li.cil.tis3d.common.module.execution.instruction.InstructionJumpLessThanZero;
import li.cil.tis3d.common.module.execution.instruction.InstructionJumpNotZero;
import li.cil.tis3d.common.module.execution.instruction.InstructionJumpRelative;
import li.cil.tis3d.common.module.execution.instruction.InstructionJumpRelativeImmediate;
import li.cil.tis3d.common.module.execution.instruction.InstructionMul;
import li.cil.tis3d.common.module.execution.instruction.InstructionMulImmediate;
import li.cil.tis3d.common.module.execution.instruction.InstructionNegate;
import li.cil.tis3d.common.module.execution.instruction.InstructionSave;
import li.cil.tis3d.common.module.execution.instruction.InstructionSubtract;
import li.cil.tis3d.common.module.execution.instruction.InstructionSubtractImmediate;
import li.cil.tis3d.common.module.execution.instruction.InstructionSwap;
import li.cil.tis3d.common.module.execution.target.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compiles TIS-100 assembly code into instructions.
 * <p>
 * Generates exceptions with line and column location if invalid code is encountered.
 */
public final class Compiler {
    /**
     * Parse the specified piece of assembly code into the specified machine state.
     * <p>
     * Note that the machine state will be hard reset.
     *
     * @param code  the code to parse and compile.
     * @param state the machine state to store the instructions and debug info in.
     * @throws ParseException if the specified code contains syntax errors.
     */
    public static void compile(final Iterable<String> code, final MachineState state) throws ParseException {
        state.clear();

        final String[] lines = Iterables.toArray(code, String.class);
        if (lines.length > Settings.maxLinesPerProgram) {
            throw new ParseException(Constants.MESSAGE_TOO_MANY_LINES, Settings.maxLinesPerProgram, 0, 0);
        }
        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            lines[lineNumber] = lines[lineNumber].toUpperCase(Locale.US);
        }

        state.code = lines;

        try {
            // Parse all lines into the specified machine state.
            final List<Validator> validators = new ArrayList<>();
            for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
                // Enforce max line length.
                if (lines[lineNumber].length() > Settings.maxColumnsPerLine) {
                    throw new ParseException(Constants.MESSAGE_TOO_MANY_COLUMNS, lineNumber, Settings.maxColumnsPerLine, Settings.maxColumnsPerLine);
                }

                // Get current line, strip comments, trim whitespace and uppercase.
                final Matcher commentMatcher = PATTERN_COMMENT.matcher(lines[lineNumber]);
                final String line = commentMatcher.replaceFirst("");

                // Extract a label, if any, pass the rest onto the instruction parser.
                final Matcher lineMatcher = PATTERN_LINE.matcher(line);
                if (lineMatcher.matches()) {
                    parseLabel(lineMatcher, state, lineNumber);
                    parseInstruction(lineMatcher, state, lineNumber, validators);
                } else {
                    // This should be pretty much impossible...
                    throw new ParseException(Constants.MESSAGE_INVALID_FORMAT, lineNumber, 0, 0);
                }
            }

            // Run all registered validators as a post-processing step. This is used
            // to check jumps reference existing labels, for example.
            for (final Validator validator : validators) {
                validator.accept(state);
            }
        } catch (final ParseException e) {
            state.clear();
            state.code = lines;
            throw e;
        }
    }

    // --------------------------------------------------------------------- //

    /**
     * Look for a label on the specified line and store it if present.
     *
     * @param matcher    the matcher for the line to parse.
     * @param state      the machine state to store the label in.
     * @param lineNumber the current line number.
     */
    private static void parseLabel(final Matcher matcher, final MachineState state, final int lineNumber) throws ParseException {
        final String label = matcher.group("label");
        if (label == null) {
            return;
        }

        // Got a label, store it and the address it represents.
        if (state.labels.containsKey(label)) {
            throw new ParseException(Constants.MESSAGE_LABEL_DUPLICATE, lineNumber, matcher.start("label"), matcher.end("label"));
        }
        state.labels.put(label, state.instructions.size());
    }

    /**
     * Look for an instruction on the specified line and store it if present.
     *
     * @param matcher    the matcher for the line to parse.
     * @param state      the machine state to store the generated instruction in.
     * @param lineNumber the number of the line we're parsing (for exceptions).
     * @param validators list of validators instruction emitters may add to.
     * @throws ParseException if there was a syntax error.
     */
    private static void parseInstruction(final Matcher matcher, final MachineState state, final int lineNumber, final List<Validator> validators) throws ParseException {
        final String name = matcher.group("name");
        if (name == null) {
            return;
        }

        // Got an instruction, process arguments and instantiate it.
        final Instruction instruction = EMITTER_MAP.getOrDefault(name, EMITTER_MISSING).
                compile(matcher, lineNumber, validators);

        // Remember line numbers for debugging.
        state.lineNumbers.put(state.instructions.size(), lineNumber);

        // Store the instruction in the machine state (after just to skip the -1 :P).
        state.instructions.add(instruction);
    }

    // --------------------------------------------------------------------- //

    private static final Pattern PATTERN_COMMENT = Pattern.compile("#.*$");
    private static final Pattern PATTERN_LINE = Pattern.compile("^\\s*(?:(?<label>[^:\\s]+)\\s*:\\s*)?(?:(?<name>\\S+)\\s*(?<arg1>[^,\\s]+)?\\s*,?\\s*(?<arg2>[^,\\s]+)?\\s*(?<excess>.+)?)?\\s*$");
    private static final Instruction INSTRUCTION_NOP = new InstructionAdd(Target.NIL);
    private static final InstructionEmitter EMITTER_MISSING = new InstructionEmitterMissing();
    private static final Map<String, InstructionEmitter> EMITTER_MAP;

    static {
        final ImmutableMap.Builder<String, InstructionEmitter> builder = ImmutableMap.builder();

        // Special handling: actually emits an `ADD NIL`.
        addInstructionEmitter(builder, new InstructionEmitterUnary("NOP", () -> INSTRUCTION_NOP));
        // Special handling: does super-special magic.
        addInstructionEmitter(builder, new InstructionEmitterUnary("HCF", InstructionHaltAndCatchFire::new));

        // Jumps.
        addInstructionEmitter(builder, new InstructionEmitterLabel("JMP", InstructionJump::new));
        addInstructionEmitter(builder, new InstructionEmitterLabel("JEZ", InstructionJumpEqualZero::new));
        addInstructionEmitter(builder, new InstructionEmitterLabel("JGZ", InstructionJumpGreaterThanZero::new));
        addInstructionEmitter(builder, new InstructionEmitterLabel("JLZ", InstructionJumpLessThanZero::new));
        addInstructionEmitter(builder, new InstructionEmitterLabel("JNZ", InstructionJumpNotZero::new));
        addInstructionEmitter(builder, new InstructionEmitterTargetOrImmediate("JRO", InstructionJumpRelative::new, InstructionJumpRelativeImmediate::new));

        // Data transfer.
        addInstructionEmitter(builder, new InstructionEmitterMove());
        addInstructionEmitter(builder, new InstructionEmitterUnary("SAV", () -> InstructionSave.INSTANCE));
        addInstructionEmitter(builder, new InstructionEmitterUnary("SWP", () -> InstructionSwap.INSTANCE));

        // Arithmetic operations.
        addInstructionEmitter(builder, new InstructionEmitterUnary("NEG", () -> InstructionNegate.INSTANCE));
        addInstructionEmitter(builder, new InstructionEmitterTargetOrImmediate("ADD", InstructionAdd::new, InstructionAddImmediate::new));
        addInstructionEmitter(builder, new InstructionEmitterTargetOrImmediate("SUB", InstructionSubtract::new, InstructionSubtractImmediate::new));
        addInstructionEmitter(builder, new InstructionEmitterTargetOrImmediate("MUL", InstructionMul::new, InstructionMulImmediate::new));
        addInstructionEmitter(builder, new InstructionEmitterTargetOrImmediate("DIV", InstructionDiv::new, InstructionDivImmediate::new));

        // Bitwise operations.
        addInstructionEmitter(builder, new InstructionEmitterUnary("NOT", () -> InstructionBitwiseNot.INSTANCE));
        addInstructionEmitter(builder, new InstructionEmitterTargetOrImmediate("AND", InstructionBitwiseAnd::new, InstructionBitwiseAndImmediate::new));
        addInstructionEmitter(builder, new InstructionEmitterTargetOrImmediate("OR", InstructionBitwiseOr::new, InstructionBitwiseOrImmediate::new));
        addInstructionEmitter(builder, new InstructionEmitterTargetOrImmediate("XOR", InstructionBitwiseXor::new, InstructionBitwiseXorImmediate::new));
        addInstructionEmitter(builder, new InstructionEmitterTargetOrImmediate("SHL", InstructionBitwiseShiftLeft::new, InstructionBitwiseShiftLeftImmediate::new));
        addInstructionEmitter(builder, new InstructionEmitterTargetOrImmediate("SHR", InstructionBitwiseShiftRight::new, InstructionBitwiseShiftRightImmediate::new));

        EMITTER_MAP = builder.build();
    }

    private static void addInstructionEmitter(final ImmutableMap.Builder<String, InstructionEmitter> builder, final InstructionEmitter emitter) {
        builder.put(emitter.getInstructionName(), emitter);
    }

    private Compiler() {
    }
}
