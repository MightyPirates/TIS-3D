package li.cil.tis3d.client.render.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.stream.IntStream;

/**
 * Base implementation for texture based font rendering.
 */
public abstract class AbstractFontRenderer implements FontRenderer {
    private final int[] CHAR_MAP = IntStream.range(0, 256).map(getCharacters()::indexOf).toArray();

    private final int COLUMNS = getResolution() / (getCharWidth() + getGapU());
    private final float U_SIZE = getCharWidth() / (float) getResolution();
    private final float V_SIZE = getCharHeight() / (float) getResolution();
    private final float U_STEP = (getCharWidth() + getGapU()) / (float) getResolution();
    private final float V_STEP = (getCharHeight() + getGapV()) / (float) getResolution();

    // --------------------------------------------------------------------- //

    public void drawString(final String value) {
        drawString(value, value.length());
    }

    public void drawString(final String value, final int maxChars) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);

        Minecraft.getMinecraft().getTextureManager().bindTexture(getTextureLocation());

        final Tessellator tessellator = Tessellator.getInstance();
        final VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        float tx = 0f;
        final int end = Math.min(maxChars, value.length());
        for (int i = 0; i < end; i++) {
            final char ch = value.charAt(i);
            drawChar(tx, ch, buffer);
            tx += getCharWidth() + getGapU();
        }

        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1);
    }

    // --------------------------------------------------------------------- //

    abstract protected String getCharacters();

    abstract protected ResourceLocation getTextureLocation();

    abstract protected int getResolution();

    abstract protected int getGapU();

    abstract protected int getGapV();

    // --------------------------------------------------------------------- //

    private void drawChar(final float x, final char ch, final VertexBuffer buffer) {
        if (Character.isWhitespace(ch) || Character.isISOControl(ch)) {
            return;
        }
        final int index = getCharIndex(ch);

        final int column = index % COLUMNS;
        final int row = index / COLUMNS;
        final float u = column * U_STEP;
        final float v = row * V_STEP;

        buffer.pos(x, getCharHeight(), 0).tex(u, v + V_SIZE).endVertex();
        buffer.pos(x + getCharWidth(), getCharHeight(), 0).tex(u + U_SIZE, v + V_SIZE).endVertex();
        buffer.pos(x + getCharWidth(), 0, 0).tex(u + U_SIZE, v).endVertex();
        buffer.pos(x, 0, 0).tex(u, v).endVertex();
    }

    private int getCharIndex(final char ch) {
        if (ch >= CHAR_MAP.length) {
            return CHAR_MAP['?'];
        }
        final int index = CHAR_MAP[ch];
        if (index < 0) {
            return CHAR_MAP['?'];
        }
        return index;
    }
}
