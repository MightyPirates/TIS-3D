package li.cil.tis3d.common.module.execution.compiler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import li.cil.tis3d.common.config.CommonConfig;
import li.cil.tis3d.common.config.Constants;
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
        if (lines.length > CommonConfig.maxLinesPerProgram && CommonConfig.maxLinesPerProgram > 0) {
            throw new ParseException(Strings.MESSAGE_TOO_MANY_LINES, CommonConfig.maxLinesPerProgram, 0, 0);
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
                if (lines[lineNumber].length() > Constants.MAX_CHARS_PER_LINE) {
                    throw new ParseException(Strings.MESSAGE_TOO_MANY_COLUMNS, lineNumber, Constants.MAX_CHARS_PER_LINE, Constants.MAX_CHARS_PER_LINE);
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
                    throw new ParseException(Strings.MESSAGE_INVALID_FORMAT, lineNumber, 0, 0);
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
    private static void parseDefine(final Matcher matcher, final Map<String, String> defines) {
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
    private static void parseUndefine(final Matcher matcher, final Map<String, String> defines) {
        final String key = matcher.group("key");
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
            throw new ParseException(Strings.MESSAGE_LABEL_DUPLICATE, lineNumber, matcher.start("label"), matcher.end("label"));
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
    private static void parseInstruction(final Matcher matcher, final MachineState state, final int lineNumber, final Map<String, String> defines, final List<Validator> validators) throws ParseException {
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
    private static final String INSTRUCTION_NO_NAME = "NOP";
    private static final Instruction INSTRUCTION_NOP = new AddInstruction(Target.NIL);
    private static final InstructionEmitter EMITTER_MISSING = new MissingInstructionEmitter();
    private static final Map<String, InstructionEmitter> EMITTER_MAP;

    static {
        final ImmutableMap.Builder<String, InstructionEmitter> builder = ImmutableMap.builder();

        // Special handling: actually emits an `ADD NIL`.
        builder.put(INSTRUCTION_NO_NAME, new UnaryInstructionEmitter(() -> INSTRUCTION_NOP));
        // Special handling: does super-special magic.
        builder.put(HaltAndCatchFireInstruction.NAME, new UnaryInstructionEmitter(() -> HaltAndCatchFireInstruction.INSTANCE));

        // Jumps.
        builder.put(JumpInstruction.NAME, new LabelInstructionEmitter(JumpInstruction::new));
        builder.put(JumpEqualZeroInstruction.NAME, new LabelInstructionEmitter(JumpEqualZeroInstruction::new));
        builder.put(JumpGreaterThanZeroInstruction.NAME, new LabelInstructionEmitter(JumpGreaterThanZeroInstruction::new));
        builder.put(JumpLessThanZeroInstruction.NAME, new LabelInstructionEmitter(JumpLessThanZeroInstruction::new));
        builder.put(JumpNotZeroInstruction.NAME, new LabelInstructionEmitter(JumpNotZeroInstruction::new));
        builder.put(JumpRelativeInstruction.NAME, new TargetOrImmediateInstructionEmitter(JumpRelativeInstruction::new, JumpRelativeImmediateInstruction::new));
        builder.put(JumpAbsoluteInstruction.NAME, new TargetOrImmediateInstructionEmitter(JumpAbsoluteInstruction::new, JumpAbsoluteImmediateInstruction::new));

        // Data transfer.
        builder.put(MoveInstruction.NAME, new MoveInstructionEmitter());
        builder.put(SaveInstruction.NAME, new UnaryInstructionEmitter(() -> SaveInstruction.INSTANCE));
        builder.put(SwapInstruction.NAME, new UnaryInstructionEmitter(() -> SwapInstruction.INSTANCE));

        // Arithmetic operations.
        builder.put(NegateInstruction.NAME, new UnaryInstructionEmitter(() -> NegateInstruction.INSTANCE));
        builder.put(AddInstruction.NAME, new TargetOrImmediateInstructionEmitter(AddInstruction::new, AddImmediateInstruction::new));
        builder.put(SubtractInstruction.NAME, new TargetOrImmediateInstructionEmitter(SubtractInstruction::new, SubtractImmediateInstruction::new));
        builder.put(MulInstruction.NAME, new TargetOrImmediateInstructionEmitter(MulInstruction::new, MulImmediateInstruction::new));
        builder.put(DivInstruction.NAME, new TargetOrImmediateInstructionEmitter(DivInstruction::new, DivImmediateInstruction::new));
        builder.put(ModuloInstruction.NAME, new TargetOrImmediateInstructionEmitter(ModuloInstruction::new, ModuloImmediateInstruction::new));

        // Bitwise operations.
        builder.put(BitwiseNotInstruction.NAME, new UnaryInstructionEmitter(() -> BitwiseNotInstruction.INSTANCE));
        builder.put(BitwiseAndInstruction.NAME, new TargetOrImmediateInstructionEmitter(BitwiseAndInstruction::new, BitwiseAndImmediateInstruction::new));
        builder.put(BitwiseOrInstruction.NAME, new TargetOrImmediateInstructionEmitter(BitwiseOrInstruction::new, BitwiseOrImmediateInstruction::new));
        builder.put(BitwiseXorInstruction.NAME, new TargetOrImmediateInstructionEmitter(BitwiseXorInstruction::new, BitwiseXorImmediateInstruction::new));
        builder.put(BitwiseShiftLeftInstruction.NAME, new TargetOrImmediateInstructionEmitter(BitwiseShiftLeftInstruction::new, BitwiseShiftLeftImmediateInstruction::new));
        builder.put(BitwiseShiftRightInstruction.NAME, new TargetOrImmediateInstructionEmitter(BitwiseShiftRightInstruction::new, BitwiseShiftRightImmediateInstruction::new));

        // Operations on LAST.
        builder.put(LastRotateLeftInstruction.NAME, new UnaryInstructionEmitter(() -> LastRotateLeftInstruction.INSTANCE));
        builder.put(LastRotateRightInstruction.NAME, new UnaryInstructionEmitter(() -> LastRotateRightInstruction.INSTANCE));

        EMITTER_MAP = builder.build();
    }

    private Compiler() {
    }
}
