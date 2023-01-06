package li.cil.tis3d.common.config;

import java.util.regex.Pattern;

/**
 * Collection of constants used throughout the mod.
 */
public final class Constants {
    // --------------------------------------------------------------------- //
    // Code book

    public static final int MAX_CHARS_PER_LINE = 30;
    public static final int MAX_LINES_PER_PAGE = 21;
    public static final Pattern PATTERN_LINES = Pattern.compile("\r?\n");

    // --------------------------------------------------------------------- //

    private Constants() {
    }
}
