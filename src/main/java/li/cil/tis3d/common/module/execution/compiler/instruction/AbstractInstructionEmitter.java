package li.cil.tis3d.common.module.execution.compiler.instruction;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.module.execution.target.Target;

import java.util.regex.Matcher;

/**
 * Base implementation for instruction emitters.
 */
abstract class AbstractInstructionEmitter implements InstructionEmitter {
    protected static void checkExcess(final int lineNumber, final Matcher matcher, final String name) throws ParseException {
        final int start = matcher.start(name);
        if (start >= 0) {
            throw new ParseException(Constants.MESSAGE_PARAMETER_OVERFLOW, lineNumber, start, matcher.end());
        }
    }

    protected static String checkArg(final int lineNumber, final Matcher matcher, final String name, final String previous) throws ParseException {
        final String arg = matcher.group(name);
        if (arg == null) {
            throw new ParseException(Constants.MESSAGE_PARAMETER_UNDERFLOW, lineNumber, matcher.end(previous) + 1, matcher.end(previous) + 1);
        }
        return arg;
    }

    protected static Target checkTarget(final int lineNumber, final String name, final int start, final int end) throws ParseException {
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

    protected static Object checkTargetOrNumber(final int lineNumber, final String name, final int start, final int end) throws ParseException {
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
}
