package li.cil.tis3d.util;

public final class Color {
    public static final int WHITE = 0xFFFFFFFF;
    public static final int ORANGE = 0xFFFFCC33;
    public static final int MAGENTA = 0xFFCC66CC;
    public static final int LIGHT_BLUE = 0xFF6699FF;
    public static final int YELLOW = 0xFFFFFF33;
    public static final int LIME = 0xFF33CC33;
    public static final int PINK = 0xFFFF6699;
    public static final int GRAY = 0xFF333333;
    public static final int LIGHT_GRAY = 0xFFCCCCCC;
    public static final int CYAN = 0xFF336699;
    public static final int PURPLE = 0xFF9933CC;
    public static final int BLUE = 0xFF333399;
    public static final int BROWN = 0xFF663300;
    public static final int GREEN = 0xFF336600;
    public static final int RED = 0xFFFF3333;
    public static final int BLACK = 0xFF000000;

    public static final int DARK_GRAY = 0xFF111111;

    public static final int GUI_TEXT = 0xFF404040;

    /**
     * Mapping of regular Minecraft dye indices to colors as ARGB.
     */
    private static final int[] COLORS = new int[]{
        WHITE, // 0: White
        ORANGE, // 1: Orange
        MAGENTA, // 2: Magenta
        LIGHT_BLUE, // 3: Light Blue
        YELLOW, // 4: Yellow
        LIME, // 5: Lime
        PINK, // 6: Pink
        GRAY, // 7: Gray
        LIGHT_GRAY, // 8: Silver
        CYAN, // 9: Cyan
        PURPLE, // 10: Purple
        BLUE, // 11: Blue
        BROWN, // 12: Brown
        GREEN, // 13: Green
        RED, // 14: Red
        BLACK  // 15: Black
    };

    /**
     * Get an ARGB color value for the dye color with the specified index.
     *
     * @param index the index to get the color for.
     * @return the color for that index as ARGB.
     */
    public static int getColorByIndex(final int index) {
        return COLORS[index % COLORS.length];
    }

    public static int withAlpha(final int color, final float alpha) {
        return ((int) (Math.max(0, Math.min(1, alpha)) * 0xFF) << 24) | (color & 0xFFFFFF);
    }

    public static int monochrome(final int brightness) {
        final int component = Math.max(0, Math.min(0xFF, brightness));
        return 0xFF000000 | (brightness << 16) | (brightness << 8) | brightness;
    }

    public static int monochrome(final float brightness) {
        return monochrome((int) (brightness * 0xFF));
    }

    /**
     * Get the alpha component of an ARGB color as an int in [0, 255].
     *
     * @param color the color to get the blue component for.
     * @return the blue component as the color in [0, 255].
     */
    public static int getAlphaU8(final int color) {
        return (color >>> 24) & 0xFF;
    }

    /**
     * Get the red component of an ARGB color as an int in [0, 255].
     *
     * @param color the color to get the red component for.
     * @return the red component as the color in [0, 255].
     */
    public static int getRedU8(final int color) {
        return (color >>> 16) & 0xFF;
    }

    /**
     * Get the alpha component of an ARGB color as an int in [0, 255].
     *
     * @param color the color to get the green component for.
     * @return the green component as the color in [0, 255].
     */
    public static int getGreenU8(final int color) {
        return (color >>> 8) & 0xFF;
    }

    /**
     * Get the blue component of an ARGB color as an int in [0, 255].
     *
     * @param color the color to get the blue component for.
     * @return the blue component as the color in [0, 255].
     */
    public static int getBlueU8(final int color) {
        return color & 0xFF;
    }

    // --------------------------------------------------------------------- //

    private Color() {
    }
}
