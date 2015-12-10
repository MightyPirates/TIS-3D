package li.cil.tis3d.client.render.font;

import li.cil.tis3d.api.API;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.stream.IntStream;

public final class FontRendererTextureMonospace {
    public static final int CHAR_WIDTH = 3;
    public static final int CHAR_HEIGHT = 4;

    private static final ResourceLocation LOCATION_FONT_TEXTURE = new ResourceLocation(API.MOD_ID, "textures/blocks/overlay/moduleExecutionFont.png");
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890:#-,?+!=()'.";
    private static final int[] CHAR_MAP = IntStream.range(0, 256).map(CHARS::indexOf).toArray();

    private static final int COLUMNS = 8;
    private static final float U_SIZE = CHAR_WIDTH / 32f;
    private static final float V_SIZE = CHAR_HEIGHT / 32f;
    private static final float U_STEP = (CHAR_WIDTH + 1) / 32f;
    private static final float V_STEP = (CHAR_HEIGHT + 1) / 32f;

    public static void drawString(final String value) {
        drawString(value, value.length());
    }

    public static void drawString(final String value, final int maxChars) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(false);

        Minecraft.getMinecraft().getTextureManager().bindTexture(LOCATION_FONT_TEXTURE);

        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        float tx = 0f;
        final int end = Math.min(maxChars, value.length());
        for (int i = 0; i < end; i++) {
            final char ch = value.charAt(i);
            drawChar(tx, ch, worldRenderer);
            tx += CHAR_WIDTH + 1;
        }

        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1);
    }

    private static void drawChar(final float x, final char ch, final WorldRenderer worldRenderer) {
        if (Character.isWhitespace(ch) || Character.isISOControl(ch)) {
            return;
        }
        final int index = getCharIndex(ch);

        final int column = index % COLUMNS;
        final int row = index / COLUMNS;
        final float u = column * U_STEP;
        final float v = row * V_STEP;

        worldRenderer.pos(x, CHAR_HEIGHT, 0).tex(u, v + V_SIZE).endVertex();
        worldRenderer.pos(x + CHAR_WIDTH, CHAR_HEIGHT, 0).tex(u + U_SIZE, v + V_SIZE).endVertex();
        worldRenderer.pos(x + CHAR_WIDTH, 0, 0).tex(u + U_SIZE, v).endVertex();
        worldRenderer.pos(x, 0, 0).tex(u, v).endVertex();
    }

    private static int getCharIndex(final char ch) {
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
