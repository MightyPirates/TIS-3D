package li.cil.tis3d.client.render.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Base interface for font renderers.
 */
@Environment(EnvType.CLIENT)
public interface FontRenderer {
    /**
     * Render the specified string.
     *
     * @param value the string to render.
     */
    void drawString(final CharSequence value);

    /**
     * Render up to the specified amount of characters of the specified string.
     * <p>
     * This is intended as a convenience method for clamped-width rendering,
     * avoiding additional string operations such as <tt>substring</tt>.
     *
     * @param value    the string to render.
     * @param maxChars the maximum number of characters to render.
     */
    void drawString(final CharSequence value, final int maxChars);

    /**
     * Get the width of the characters drawn with the font renderer, in pixels.
     *
     * @return the width of the drawn characters.
     */
    int getCharWidth();

    /**
     * Get the height of the characters drawn with the font renderer, in pixels.
     *
     * @return the height of the drawn characters.
     */
    int getCharHeight();
}
