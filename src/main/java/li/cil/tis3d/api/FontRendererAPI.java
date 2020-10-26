package li.cil.tis3d.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

/**
 * API entry point for access to the tiny font renderer used on execution
 * modules, for example.
 * <p>
 * Keep in mind that the list of printable characters is very small for this
 * font renderer, due to the small number of characters in the font sheet.
 */
@Environment(EnvType.CLIENT)
public interface FontRendererAPI {
    /**
     * Font type; small font is found on eg. the execution module,
     * whereas the normal font is used for modules which require more
     * legibility, eg. the terminal module.
     */
    public enum Font {
        SmallFont,
        NormalFont
    }

    /**
     * Render the specified string.
     *
     * @param string the string to render.
     */
    void drawString(final String string);

    /**
     * Render up to the specified amount of characters of the specified string.
     * <p>
     * This is intended as a convenience method for clamped-width rendering,
     * avoiding additional string operations such as <tt>substring</tt>.
     *
     * @param string   the string to render.
     * @param maxChars the maximum number of characters to render.
     */
    void drawString(final String string, final int maxChars);

    /**
     * Render up to the specified amount of characters of the specified string.
     *
     * @param font     the font type.
     * @param matrices the current transformation.
     * @param vc       the VertexConsumer queried via {@code chooseVertexConsumer}.
     * @param light    the light value, usually {@code RenderUtil.maxLight}.
     * @param overlay  the overlay value, passed from vanilla.
     * @param color    the text color, in {@code 0xAARRGGBB}.
     * @param string   the string to render.
     * @param maxChars the maximum number of characters to render.
     */
    void drawString(final Font font, final MatrixStack.Entry matrices, final VertexConsumer vc,
                    final int light, final int overlay, final int color,
                    final CharSequence string, final int maxChars);

    /**
     * Query an appropriate {@link net.minecraft.client.render.VertexConsumer} for a given font type.
     *
     * @param font the font type.
     * @param vcp  the VertexConsumerProvider for this frame.
     * @return the VertexConsumer instance to use with further text render calls.
     */
    VertexConsumer chooseVertexConsumer(final Font font, final VertexConsumerProvider vcp);

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
