package li.cil.tis3d.common.module.execution.compiler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.module.execution.MachineState;
import li.cil.tis3d.common.module.execution.compiler.instruction.*;
import li.cil.tis3d.common.module.execution.instruction.*;
import li.cil.tis3d.common.module.execution.target.Target;

import java.util.*;
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
            final Map<String, String> defines = new HashMap<>();
            for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
                // Enforce max line length.
                if (lines[lineNumber].length() > Settings.maxColumnsPerLine) {
                    throw new ParseException(Constants.MESSAGE_TOO_MANY_COLUMNS, lineNumber, Settings.maxColumnsPerLine, Settings.maxColumnsPerLine);
                }

                // Check for defines, also trims whitespace.
                final Matcher defineMatcher = PATTERN_DEFINE.matcher(lines[lineNumber]);
                if (defineMatcher.matches()) {
                    parseDefine(defineMatcher, defines);
                }

                final Matcher undefineMatcher = PATTERN_UNDEFINE.matcher(lines[lineNumber]);
                if (undefineMatcher.matches()) {
                    parseUndefine(undefineMatcher, defines);
                }

                // Get current line, strip comments.
                final Matcher commentMatcher = PATTERN_COMMENT.matcher(lines[lineNumber]);
                final String line = commentMatcher.replaceFirst("").trim();

                // Extract a label, if any, pass the rest onto the instruction parser. Also trims.
                final Matcher lineMatcher = PATTERN_LINE.matcher(line);
                if (lineMatcher.matches()) {
                    parseLabel(lineMatcher, state, lineNumber);
                    parseInstruction(lineMatcher, state, lineNumber, defines, validators);
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
     * Parse a define from the specified match and put it in the map of defines.
     *
     * @param matcher the matcher for the line to parse.
     * @param defines the map with defines to add results to.
     */
    private static void parseDefine(Matcher matcher, Map<String, String> defines) {
        final String key = matcher.group("key");
        if (key == null) {
            return;
        }

        String value = matcher.group("value");
        if (value == null) {
            return;
        }

        if (key.equals(value)) {
            return;
        }

        // Resolve value if it is also a define.
        if (defines.containsKey(value)) {
            value = defines.get(value);
        }

        // Overwrite previous defines if there is one.
        defines.put(key, value);
    }

    /**
     * Parse an undefine from the specified match and remove it from the map of defines.
     *
     * @param matcher the matcher for the line to parse.
     * @param defines the map with defines to remove results from.
     */
    private static void parseUndefine(Matcher matcher, Map<String, String> defines) {
        String key = matcher.group("key");
        if (key == null) {
            return;
        }

        defines.remove(key);
    }

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
     * @param defines    the map of currently active defines.
     * @param validators list of validators instruction emitters may add to.
     * @throws ParseException if there was a syntax error.
     */
    private static void parseInstruction(final Matcher matcher, final MachineState state, final int lineNumber, Map<String, String> defines, final List<Validator> validators) throws ParseException {
        final String name = matcher.group("name");
        if (name == null) {
            return;
        }

        // Got an instruction, process arguments and instantiate it.
        final Instruction instruction = EMITTER_MAP.getOrDefault(name, EMITTER_MISSING).
                compile(matcher, lineNumber, defines, validators);

        // Remember line numbers for debugging.
        state.lineNumbers.put(state.instructions.size(), lineNumber);

        // Store the instruction in the machine state (after just to skip the -1 :P).
        state.instructions.add(instruction);
    }

    // --------------------------------------------------------------------- //

    private static final Pattern PATTERN_COMMENT = Pattern.compile("#.*$");
    private static final Pattern PATTERN_DEFINE = Pattern.compile("#DEFINE\\s+(?<key>\\S+)\\s*(?<value>\\S+)\\s*$");
    private static final Pattern PATTERN_UNDEFINE = Pattern.compile("#UNDEF\\s+(?<key>\\S+)\\s*$");
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

        // Operations on LAST.
        addInstructionEmitter(builder, new InstructionEmitterUnary("RLLAST", InstructionLastRotateLeft::new));
        addInstructionEmitter(builder, new InstructionEmitterUnary("RRLAST", InstructionLastRotateRight::new));

        EMITTER_MAP = builder.build();
    }

    private static void addInstructionEmitter(final ImmutableMap.Builder<String, InstructionEmitter> builder, final InstructionEmitter emitter) {
        builder.put(emitter.getInstructionName(), emitter);
    }

    private Compiler() {
    }
}
