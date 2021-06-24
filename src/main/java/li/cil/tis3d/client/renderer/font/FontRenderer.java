package li.cil.tis3d.client.renderer.font;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Base interface for font renderers.
 */
@OnlyIn(Dist.CLIENT)
public interface FontRenderer {
    /**
     * Render the specified string.
     *
     * @param matrixStack   the current matrix stack.
     * @param bufferFactory the buffer to render the string into.
     * @param value         the string to render.
     */
    void drawString(final MatrixStack matrixStack, final IRenderTypeBuffer bufferFactory, final CharSequence value);

    /**
     * Render up to the specified amount of characters of the specified string.
     * <p>
     * This is intended as a convenience method for clamped-width rendering,
     * avoiding additional string operations such as <tt>substring</tt>.
     *
     * @param matrixStack   the current matrix stack.
     * @param bufferFactory the buffer to render the string into.
     * @param value         the string to render.
     * @param argb          the color to render the string with.
     * @param maxChars      the maximum number of characters to render.
     */
    void drawString(final MatrixStack matrixStack, final IRenderTypeBuffer bufferFactory, final CharSequence value, final int argb, final int maxChars);

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
