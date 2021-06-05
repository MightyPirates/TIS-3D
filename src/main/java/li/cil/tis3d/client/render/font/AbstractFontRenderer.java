package li.cil.tis3d.client.render.font;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.util.ColorUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

/**
 * Base implementation for texture based font rendering.
 */
@Environment(EnvType.CLIENT)
public abstract class AbstractFontRenderer implements FontRenderer {
    private final Int2IntMap CHAR_MAP;

    private final int COLUMNS = getResolution() / (getCharWidth() + getGapU());
    private final float U_SIZE = getCharWidth() / (float)getResolution();
    private final float V_SIZE = getCharHeight() / (float)getResolution();
    private final float U_STEP = (getCharWidth() + getGapU()) / (float)getResolution();
    private final float V_STEP = (getCharHeight() + getGapV()) / (float)getResolution();

    private RenderLayer renderLayer;

    AbstractFontRenderer() {
        CHAR_MAP = new Int2IntOpenHashMap();
        final CharSequence chars = getCharacters();
        for (int index = 0; index < chars.length(); index++) {
            CHAR_MAP.put(chars.charAt(index), index);
        }
    }

    // --------------------------------------------------------------------- //

    @Override
    public void drawString(final CharSequence value) {
        drawString(value, value.length());
    }

    @Override
    public void drawString(final CharSequence value, final int maxChars) {
        drawString(value, maxChars);
    }

    public void drawString(final MatrixStack matrices, final CharSequence value) {
        drawString(matrices, value, value.length());
    }

    public void drawString(final MatrixStack matrices, final CharSequence value, final int maxChars) {

        matrices.push();
        RenderSystem.depthMask(false);

        MinecraftClient.getInstance().getTextureManager().bindTexture(getTextureLocation());

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        float tx = 0f;
        final int end = Math.min(maxChars, value.length());
        for (int i = 0; i < end; i++) {
            final char ch = value.charAt(i);
            drawChar(tx, ch, buffer);
            tx += getCharWidth() + getGapU();
        }

        RenderSystem.enableBlend();
        tessellator.draw();

        RenderSystem.depthMask(true);
        matrices.pop();
    }

    public void drawString(final MatrixStack.Entry matrices, final VertexConsumer vc,
                           final int light, final int overlay,
                           final CharSequence value) {
        drawString(matrices, vc, light, overlay, ColorUtils.WHITE, value, value.length());
    }

    public void drawString(final MatrixStack.Entry matrices, final VertexConsumer vc,
                           final int light, final int overlay, final int color,
                           final CharSequence value) {
        drawString(matrices, vc, light, overlay, color, value, value.length());
    }

    public void drawString(final MatrixStack.Entry matrices, final VertexConsumer vc,
                           final int light, final int overlay,
                           final CharSequence value, final int maxChars) {
        drawString(matrices, vc, light, overlay, ColorUtils.WHITE, value, maxChars);
    }

    public void drawString(final MatrixStack.Entry matrices, final VertexConsumer vc,
                           final int light, final int overlay, final int color,
                           final CharSequence value, final int maxChars) {
        float tx = 0f;
        final int end = Math.min(maxChars, value.length());
        for (int i = 0; i < end; i++) {
            final char ch = value.charAt(i);
            drawChar(matrices, vc, light, overlay, color, tx, ch);
            tx += getCharWidth() + getGapU();
        }
    }

    /**
     * Choose a VertexConsumer appropriate for the given drawing parameters.
     * The returned instance can be reused for subsequent draw calls.
     *
     * @param vcp the provider instance to choose from.
     * @return the VertexConsumer instance to pass to drawString().
     */
    public VertexConsumer chooseVertexConsumer(final VertexConsumerProvider vcp) {
        if (renderLayer == null) {
            // Use correct RenderLayer (getCutoutNoDiffLight)
            renderLayer = RenderLayer.getEntitySmoothCutout(getTextureLocation());
        }

        return vcp.getBuffer(renderLayer);
    }

    // --------------------------------------------------------------------- //

    abstract protected CharSequence getCharacters();

    abstract protected Identifier getTextureLocation();

    abstract protected int getResolution();

    abstract protected int getGapU();

    abstract protected int getGapV();

    // --------------------------------------------------------------------- //

    private void drawChar(final float x, final char ch, final BufferBuilder buffer) {
        if (Character.isWhitespace(ch) || Character.isISOControl(ch)) {
            return;
        }
        final int index = getCharIndex(ch);

        final int column = index % COLUMNS;
        final int row = index / COLUMNS;
        final float u = column * U_STEP;
        final float v = row * V_STEP;

        buffer.vertex(x, getCharHeight(), 0).texture(u, v + V_SIZE).next();
        buffer.vertex(x + getCharWidth(), getCharHeight(), 0).texture(u + U_SIZE, v + V_SIZE).next();
        buffer.vertex(x + getCharWidth(), 0, 0).texture(u + U_SIZE, v).next();
        buffer.vertex(x, 0, 0).texture(u, v).next();
    }

    private void drawChar(final MatrixStack.Entry matrices, final VertexConsumer vc,
                          final int light, final int overlay, final int color,
                          final float x, final char ch) {
        if (Character.isWhitespace(ch) || Character.isISOControl(ch)) {
            return;
        }
        final int index = getCharIndex(ch);

        final int column = index % COLUMNS;
        final int row = index / COLUMNS;
        final float u = column * U_STEP;
        final float v = row * V_STEP;

        RenderUtil.drawQuad(matrices, vc,
                            x, 0, getCharWidth(), getCharHeight(),
                            u, v, u + U_SIZE, v + V_SIZE,
                            color, light, overlay);
    }

    private int getCharIndex(final char ch) {
        if (!CHAR_MAP.containsKey(ch)) {
            return CHAR_MAP.get('?');
        }
        return CHAR_MAP.get(ch);
    }
}
