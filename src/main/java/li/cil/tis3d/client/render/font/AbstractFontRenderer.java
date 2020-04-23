package li.cil.tis3d.client.render.font;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

/**
 * Base implementation for texture based font rendering.
 */
public abstract class AbstractFontRenderer implements FontRenderer {
    private final Int2IntMap CHAR_MAP;

    private final int COLUMNS = getResolution() / (getCharWidth() + getGapU());
    private final float U_SIZE = getCharWidth() / (float)getResolution();
    private final float V_SIZE = getCharHeight() / (float)getResolution();
    private final float U_STEP = (getCharWidth() + getGapU()) / (float)getResolution();
    private final float V_STEP = (getCharHeight() + getGapV()) / (float)getResolution();

    AbstractFontRenderer() {
        CHAR_MAP = new Int2IntOpenHashMap();
        final CharSequence chars = getCharacters();
        for (int index = 0; index < chars.length(); index++) {
            CHAR_MAP.put(chars.charAt(index), index);
        }
    }

    // --------------------------------------------------------------------- //

    public void drawString(final CharSequence value) {
        drawString(value, value.length());
    }

    public void drawString(final CharSequence value, final int maxChars) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);

        MinecraftClient.getInstance().getTextureManager().bindTexture(getTextureLocation());

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);

        float tx = 0f;
        final int end = Math.min(maxChars, value.length());
        for (int i = 0; i < end; i++) {
            final char ch = value.charAt(i);
            drawChar(tx, ch, buffer);
            tx += getCharWidth() + getGapU();
        }

        GlStateManager.enableBlend();
        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
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

    private int getCharIndex(final char ch) {
        if (!CHAR_MAP.containsKey(ch)) {
            return CHAR_MAP.get('?');
        }
        return CHAR_MAP.get(ch);
    }
}
