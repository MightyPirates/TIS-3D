package li.cil.tis3d.util;

public final class ColorUtils {
    /**
     * Mapping of regular Minecraft dye indices to colors as ARGB.
     */
    private static final int[] COLORS = new int[]{
        0xFFFFFFFF, // 0: White
        0xFFFFCC33, // 1: Orange
        0xFFCC66CC, // 2: Magenta
        0xFF6699FF, // 3: Light Blue
        0xFFFFFF33, // 4: Yellow
        0xFF33CC33, // 5: Lime
        0xFFFF6699, // 6: Pink
        0xFF333333, // 7: Gray
        0xFFCCCCCC, // 8: Silver
        0xFF336699, // 9: Cyan
        0xFF9933CC, // 10: Purple
        0xFF333399, // 11: Blue
        0xFF663300, // 12: Brown
        0xFF336600, // 13: Green
        0xFFFF3333, // 14: Red
        0xFF000000  // 15: Black
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

    /**
     * Get the alpha component of an ARGB color as a float in [0, 1].
     *
     * @param color the color to get the alpha component for.
     * @return the alpha component as the color in [0, 1].
     */
    public static float getAlpha(final int color) {
        final int r = (color >>> 24) & 0xFF;
        return r / 255f;
    }

    /**
     * Get the red component of an ARGB color as a float in [0, 1].
     *
     * @param color the color to get the red component for.
     * @return the red component as the color in [0, 1].
     */
    public static float getRed(final int color) {
        return getRedU8(color) / 255f;
    }

    /**
     * Get the alpha component of an ARGB color as a float in [0, 1].
     *
     * @param color the color to get the green component for.
     * @return the green component as the color in [0, 1].
     */
    public static float getGreen(final int color) {
        return getGreenU8(color) / 255f;
    }

    /**
     * Get the blue component of an ARGB color as a float in [0, 1].
     *
     * @param color the color to get the blue component for.
     * @return the blue component as the color in [0, 1].
     */
    public static float getBlue(final int color) {
        return getBlueU8(color) / 255f;
    }

    // --------------------------------------------------------------------- //

    private ColorUtils() {
    }
}
