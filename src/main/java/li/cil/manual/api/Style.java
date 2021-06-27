package li.cil.manual.api;

import li.cil.manual.api.render.FontRenderer;

/**
 * Style definition used for rendering manuals.
 */
public interface Style {
    /**
     * The text color to use when rendering regular text.
     *
     * @return the regular text color.
     */
    int getRegularTextColor();

    /**
     * The text color to use when rendering mono-spaced text.
     *
     * @return the monospace text color.
     */
    int getMonospaceTextColor();

    /**
     * Regular color of links.
     *
     * @return color of links.
     */
    int getRegularLinkColor();

    /**
     * Color of links while hovered by the cursor.
     *
     * @return hovered link color.
     */
    int getHoveredLinkColor();

    /**
     * Regular color of  dead links.
     *
     * @return color of dead links.
     */
    int getRegularDeadLinkColor();

    /**
     * Color of dead links while hovered by the cursor.
     *
     * @return hovered dead link color.
     */
    int getHoveredDeadLinkColor();

    /**
     * The font to use for rendering regular text.
     *
     * @return the regular font.
     */
    FontRenderer getRegularFont();

    /**
     * The font to use for rendering mono-spaced text.
     *
     * @return the monospace font.
     */
    FontRenderer getMonospaceFont();

    /**
     * The line height for lines in the manual.
     * <p>
     * Fonts will be scaled to match their inherent line height to this value.
     *
     * @return the regular line height.
     */
    default int getLineHeight() {
        return getRegularFont().lineHeight();
    }
}
