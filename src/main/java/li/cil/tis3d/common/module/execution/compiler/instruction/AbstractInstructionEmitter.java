package li.cil.tis3d.common.module.execution.compiler.instruction;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.module.execution.target.Target;

import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Base implementation for instruction emitters.
 */
abstract class AbstractInstructionEmitter implements InstructionEmitter {
    static void checkExcess(final int lineNumber, final Matcher matcher, final String name) throws ParseException {
        final int start = matcher.start(name);
        if (start >= 0) {
            throw new ParseException(Constants.MESSAGE_PARAMETER_OVERFLOW, lineNumber, start, matcher.end());
        }
    }

    static String checkArg(final int lineNumber, final Matcher matcher, final String name, final String previous) throws ParseException {
        final String arg = matcher.group(name);
        if (arg == null) {
            throw new ParseException(Constants.MESSAGE_PARAMETER_UNDERFLOW, lineNumber, matcher.end(previous) + 1, matcher.end(previous) + 1);
        }
        return arg;
    }

    static Target checkTarget(String name, final int lineNumber, final Map<String, String> defines, final int start, final int end) throws ParseException {
        name = applyDefines(name, lineNumber, defines, start, end);
        try {
            final Target target = Enum.valueOf(Target.class, name);
            if (!Target.VALID_TARGETS.contains(target)) {
                throw new ParseException(Constants.MESSAGE_PARAMETER_INVALID, lineNumber, start, end);
            }
            return target;
        } catch (final IllegalArgumentException ex) {
            throw new ParseException(Constants.MESSAGE_PARAMETER_INVALID, lineNumber, start, end);
        }
    }

    static Object checkTargetOrNumber(String name, final int lineNumber, final Map<String, String> defines, final int start, final int end) throws ParseException {
        name = applyDefines(name, lineNumber, defines, start, end);
        try {
            final Target target = Enum.valueOf(Target.class, name);
            if (!Target.VALID_TARGETS.contains(target)) {
                throw new ParseException(Constants.MESSAGE_PARAMETER_INVALID, lineNumber, start, end);
            }
            return target;
        } catch (final IllegalArgumentException ex) {
            try {
                return Integer.decode(name).shortValue();
            } catch (final NumberFormatException ignored) {
                throw new ParseException(Constants.MESSAGE_PARAMETER_INVALID, lineNumber, start, end);
            }
        }
    }

    private static String applyDefines(String value, final int lineNumber, final Map<String, String> defines, final int start, final int end) throws ParseException {
        final HashSet<String> seen = new HashSet<>();
        while (defines.containsKey(value)) {
            if (!seen.add(value)) {
                throw new ParseException(Constants.MESSAGE_CYCLIC_DEFINE, lineNumber, start, end);
            }
            value = defines.get(value);
        }

        return value;
    }
}
