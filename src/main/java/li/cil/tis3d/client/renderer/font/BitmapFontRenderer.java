package li.cil.tis3d.client.renderer.font;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import li.cil.manual.api.render.FontRenderer;
import li.cil.tis3d.client.renderer.ModRenderType;
import li.cil.tis3d.util.Color;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;

import java.util.Optional;

/**
 * Base implementation for texture based font rendering.
 */
public abstract class BitmapFontRenderer implements FontRenderer {
    private final Char2IntMap CHAR_MAP;

    private final int COLUMNS = getResolution() / (charWidth() + getGapU());
    private final float U_SIZE = charWidth() / (float) getResolution();
    private final float V_SIZE = lineHeight() / (float) getResolution();
    private final float U_STEP = (charWidth() + getGapU()) / (float) getResolution();
    private final float V_STEP = (lineHeight() + getGapV()) / (float) getResolution();

    private RenderType renderLayer;

    BitmapFontRenderer() {
        CHAR_MAP = new Char2IntOpenHashMap();
        final CharSequence chars = getCharacters();
        for (int index = 0; index < chars.length(); index++) {
            CHAR_MAP.put(chars.charAt(index), index);
        }
    }

    // --------------------------------------------------------------------- //

    public void drawBatch(final MatrixStack matrixStack, final IRenderTypeBuffer bufferFactory, final CharSequence value, final int argb) {
        final IVertexBuilder buffer = getDefaultBuffer(bufferFactory);

        float tx = 0f;
        for (int i = 0; i < value.length(); i++) {
            final char ch = value.charAt(i);
            drawChar(matrixStack, buffer, argb, tx, ch);
            tx += width(" ") + getGapU();
        }
    }

    @Override
    public int width(final CharSequence value) {
        return value.length() * charWidth();
    }

    @Override
    public int width(final ITextComponent value) {
        final MutableInteger count = new MutableInteger();
        value.visit(s -> {
            count.value += s.length() * charWidth();
            return Optional.empty();
        });
        return count.value;
    }

    // --------------------------------------------------------------------- //

    protected abstract CharSequence getCharacters();

    protected abstract ResourceLocation getTextureLocation();

    protected abstract int getResolution();

    protected abstract int getGapU();

    protected abstract int getGapV();

    protected abstract int charWidth();

    // --------------------------------------------------------------------- //

    private IVertexBuilder getDefaultBuffer(final IRenderTypeBuffer bufferFactory) {
        if (renderLayer == null) {
            renderLayer = ModRenderType.unlitTexture(getTextureLocation());
        }

        return bufferFactory.getBuffer(renderLayer);
    }

    private void drawChar(final MatrixStack matrixStack, final IVertexBuilder buffer, final int argb, final float x, final char ch) {
        if (Character.isWhitespace(ch) || Character.isISOControl(ch)) {
            return;
        }

        final int index = getCharIndex(ch);

        final int a = Color.getAlphaU8(argb);
        final int r = Color.getRedU8(argb);
        final int g = Color.getGreenU8(argb);
        final int b = Color.getBlueU8(argb);

        final int column = index % COLUMNS;
        final int row = index / COLUMNS;
        final float u = column * U_STEP;
        final float v = row * V_STEP;

        final Matrix4f matrix = matrixStack.last().pose();
        buffer.vertex(matrix, x, lineHeight(), 0)
            .color(r, g, b, a)
            .uv(u, v + V_SIZE)
            .endVertex();
        buffer.vertex(matrix, x + charWidth(), lineHeight(), 0)
            .color(r, g, b, a)
            .uv(u + U_SIZE, v + V_SIZE)
            .endVertex();
        buffer.vertex(matrix, x + charWidth(), 0, 0)
            .color(r, g, b, a)
            .uv(u + U_SIZE, v)
            .endVertex();
        buffer.vertex(matrix, x, 0, 0)
            .color(r, g, b, a)
            .uv(u, v)
            .endVertex();
    }

    private int getCharIndex(final char ch) {
        if (!CHAR_MAP.containsKey(ch)) {
            return CHAR_MAP.get('?');
        }
        return CHAR_MAP.get(ch);
    }

    // --------------------------------------------------------------------- //

    private static final class MutableInteger {
        public int value;
    }
}
