package li.cil.tis3d.client.renderer.font;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import li.cil.tis3d.client.renderer.RenderLayerAccess;
import li.cil.tis3d.util.Color;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

/**
 * Base implementation for texture based font rendering.
 */
public abstract class AbstractFontRenderer implements FontRenderer {
    private final Char2IntMap CHAR_MAP;

    private final int COLUMNS = getResolution() / (getCharWidth() + getGapU());
    private final float U_SIZE = getCharWidth() / (float) getResolution();
    private final float V_SIZE = getCharHeight() / (float) getResolution();
    private final float U_STEP = (getCharWidth() + getGapU()) / (float) getResolution();
    private final float V_STEP = (getCharHeight() + getGapV()) / (float) getResolution();

    private RenderType renderLayer;

    AbstractFontRenderer() {
        CHAR_MAP = new Char2IntOpenHashMap();
        final CharSequence chars = getCharacters();
        for (int index = 0; index < chars.length(); index++) {
            CHAR_MAP.put(chars.charAt(index), index);
        }
    }

    // --------------------------------------------------------------------- //

    public void drawString(final MatrixStack matrixStack, final IRenderTypeBuffer bufferFactory, final CharSequence value) {
        drawString(matrixStack, bufferFactory, value, Color.WHITE, value.length());
    }

    public void drawString(final MatrixStack matrixStack, final IRenderTypeBuffer bufferFactory, final CharSequence value, final int color, final int maxChars) {
        final IVertexBuilder buffer = getDefaultBuffer(bufferFactory);

        float tx = 0f;
        final int end = Math.min(maxChars, value.length());
        for (int i = 0; i < end; i++) {
            final char ch = value.charAt(i);
            drawChar(matrixStack, buffer, color, tx, ch);
            tx += getCharWidth() + getGapU();
        }
    }

    // --------------------------------------------------------------------- //

    abstract protected CharSequence getCharacters();

    abstract protected ResourceLocation getTextureLocation();

    abstract protected int getResolution();

    abstract protected int getGapU();

    abstract protected int getGapV();

    // --------------------------------------------------------------------- //

    private IVertexBuilder getDefaultBuffer(final IRenderTypeBuffer bufferFactory) {
        if (renderLayer == null) {
            renderLayer = RenderLayerAccess.getModuleOverlay(getTextureLocation());
        }

        return bufferFactory.getBuffer(renderLayer);
    }

    private void drawChar(final MatrixStack matrixStack, final IVertexBuilder buffer, final int color, final float x, final char ch) {
        if (Character.isWhitespace(ch) || Character.isISOControl(ch)) {
            return;
        }

        final int index = getCharIndex(ch);

        final int a = (color >>> 24) & 0xFF;
        final int r = (color >>> 16) & 0xFF;
        final int g = (color >>> 8) & 0xFF;
        final int b = color & 0xFF;

        final int column = index % COLUMNS;
        final int row = index / COLUMNS;
        final float u = column * U_STEP;
        final float v = row * V_STEP;

        final Matrix4f matrix = matrixStack.last().pose();
        buffer.vertex(matrix, x, getCharHeight(), 0)
            .color(r, g, b, a)
            .uv(u, v + V_SIZE)
            .endVertex();
        buffer.vertex(matrix, x + getCharWidth(), getCharHeight(), 0)
            .color(r, g, b, a)
            .uv(u + U_SIZE, v + V_SIZE)
            .endVertex();
        buffer.vertex(matrix, x + getCharWidth(), 0, 0)
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
}
