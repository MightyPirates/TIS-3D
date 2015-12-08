package li.cil.tis3d.system.module.execution.compiler.instruction;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.system.module.execution.compiler.ParseException;
import li.cil.tis3d.system.module.execution.target.Target;

import java.util.regex.Matcher;

/**
 * Base implementation for instruction emitters.
 */
abstract class AbstractInstructionEmitter implements InstructionEmitter {
    protected static void checkExcess(final int lineNumber, final Matcher matcher, final String name) throws ParseException {
        final int start = matcher.start(name);
        if (start >= 0) {
            throw new ParseException(Constants.MESSAGE_EXCESS_TOKENS, lineNumber, start);
        }
    }

    protected static String checkArg(final int lineNumber, final Matcher matcher, final String name, final String previous) throws ParseException {
        final String arg = matcher.group(name);
        if (arg == null) {
            throw new ParseException(Constants.MESSAGE_MISSING_PARAMETER, lineNumber, matcher.end(previous) + 1);
        }
        return arg;
    }

    protected static Target checkTarget(final int lineNumber, final String name, final int column) throws ParseException {
        try {
            final Target target = Enum.valueOf(Target.class, name);
            if (!Target.VALID_TARGETS.contains(target)) {
                throw new ParseException(Constants.MESSAGE_INVALID_TARGET, lineNumber, column);
            }
            return target;
        } catch (final IllegalArgumentException ex) {
            throw new ParseException(Constants.MESSAGE_INVALID_TARGET, lineNumber, column);
        }
    }

    protected static Object checkTargetOrInt(final int lineNumber, final String name, final int column) throws ParseException {
        try {
            final Target target = Enum.valueOf(Target.class, name);
            if (!Target.VALID_TARGETS.contains(target)) {
                throw new ParseException(Constants.MESSAGE_INVALID_TARGET, lineNumber, column);
            }
            return target;
        } catch (final IllegalArgumentException ex) {
            if (isInteger(name)) {
                return Integer.decode(name);
            }
            throw new ParseException(Constants.MESSAGE_INVALID_TARGET, lineNumber, column);
        }
    }

    private static boolean isInteger(final String value) {
        try {
            Integer.decode(value);
            return true;
        } catch (final NumberFormatException ex) {
            return false;
        }
    }
}
