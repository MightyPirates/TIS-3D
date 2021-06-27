package li.cil.manual.api.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Base interface for font renderers.
 */
@OnlyIn(Dist.CLIENT)
public interface FontRenderer {
    /**
     * Render up to the specified amount of characters of the specified string.
     *
     * @param matrixStack the current matrix stack.
     * @param buffer      the buffer to render the string into.
     * @param value       the string to render.
     * @param argb        the color to render the string with.
     */
    void drawBatch(final MatrixStack matrixStack, final IRenderTypeBuffer buffer, final CharSequence value, final int argb);

    /**
     * Draws a string in immediate mode.
     *
     * @param matrixStack the current matrix stack.
     * @param value       the string to render.
     * @param argb        the color to render the string with.
     */
    default void draw(final MatrixStack matrixStack, final CharSequence value, final int argb) {
        final BufferBuilder builder = Tessellator.getInstance().getBuilder();
        final IRenderTypeBuffer.Impl buffer = IRenderTypeBuffer.immediate(builder);
        drawBatch(matrixStack, buffer, value, argb);
        buffer.endBatch();
    }

    /**
     * Computes the rendered width of the provided character sequence.
     *
     * @param value the value to get the render width for.
     * @return the render width of the specified value.
     */
    int width(final CharSequence value);

    /**
     * Computes the rendered width of the provided text component.
     *
     * @param value the value to get the render width for.
     * @return the render width of the specified value.
     */
    int width(final ITextComponent value);

    /**
     * Get the height of the characters drawn with the font renderer, in pixels.
     *
     * @return the height of the drawn characters.
     */
    int lineHeight();
}
