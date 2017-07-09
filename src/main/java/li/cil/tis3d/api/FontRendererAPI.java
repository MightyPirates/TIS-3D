package li.cil.tis3d.api;

/**
 * API entry point for access to the tiny font renderer used on execution
 * modules, for example.
 * <p>
 * Keep in mind that the list of printable characters is very small for this
 * font renderer, due to the small number of characters in the font sheet.
 */
public final class FontRendererAPI {
    /**
     * Render the specified string.
     *
     * @param string the string to render.
     */
    public static void drawString(final String string) {
        if (API.fontRendererAPI != null) {
            API.fontRendererAPI.drawString(string);
        }
    }

    /**
     * Render up to the specified amount of characters of the specified string.
     * <p>
     * This is intended as a convenience method for clamped-width rendering,
     * avoiding additional string operations such as <tt>substring</tt>.
     *
     * @param string   the string to render.
     * @param maxChars the maximum number of characters to render.
     */
    public static void drawString(final String string, final int maxChars) {
        if (API.fontRendererAPI != null) {
            API.fontRendererAPI.drawString(string, maxChars);
        }
    }

    /**
     * Get the width of the characters drawn with the font renderer, in pixels.
     *
     * @return the width of the drawn characters.
     */
    public static int getCharWidth() {
        if (API.fontRendererAPI != null) {
            return API.fontRendererAPI.getCharWidth();
        }
        return 0;
    }

    /**
     * Get the height of the characters drawn with the font renderer, in pixels.
     *
     * @return the height of the drawn characters.
     */
    public static int getCharHeight() {
        if (API.fontRendererAPI != null) {
            return API.fontRendererAPI.getCharHeight();
        }
        return 0;
    }

    // --------------------------------------------------------------------- //

    private FontRendererAPI() {
    }
}
