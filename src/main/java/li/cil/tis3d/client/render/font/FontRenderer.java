package li.cil.tis3d.client.render.font;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

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

    public void drawString(final MatrixStack.Entry matrices, final VertexConsumer vc,
                           final int light, final int overlay, final int color,
                           final CharSequence value, final int maxChars);

    public VertexConsumer chooseVertexConsumer(final VertexConsumerProvider vcp);

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
