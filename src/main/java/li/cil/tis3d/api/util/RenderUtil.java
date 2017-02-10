package li.cil.tis3d.api.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * Utility class for rendering related operations.
 */
public final class RenderUtil {
    /**
     * Bind the texture at the specified location to be used for quad rendering.
     *
     * @param location the location of the texture to bind.
     */
    @SideOnly(Side.CLIENT)
    public static void bindTexture(final ResourceLocation location) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
    }

    /**
     * Draw a full one-by-one, untextured quad.
     *
     * @param x the x position of the quad.
     * @param y the y position of the quad.
     * @param w the width of the quad.
     * @param h the height of the quad.
     */
    @SideOnly(Side.CLIENT)
    public static void drawUntexturedQuad(final float x, final float y, final float w, final float h) {
        final Tessellator tessellator = Tessellator.getInstance();
        final VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buffer.pos(x, y + h, 0).endVertex();
        buffer.pos(x + w, y + h, 0).endVertex();
        buffer.pos(x + w, y, 0).endVertex();
        buffer.pos(x, y, 0).endVertex();
        tessellator.draw();
    }

    /**
     * Draw an arbitrarily sized quad with the specified texture coordinates.
     *
     * @param x  the x position of the quad.
     * @param y  the y position of the quad.
     * @param w  the width of the quad.
     * @param h  the height of the quad.
     * @param u0 lower u texture coordinate.
     * @param v0 lower v texture coordinate.
     * @param u1 upper u texture coordinate.
     * @param v1 upper v texture coordinate.
     */
    @SideOnly(Side.CLIENT)
    public static void drawQuad(final float x, final float y, final float w, final float h, final float u0, final float v0, final float u1, final float v1) {
        final Tessellator tessellator = Tessellator.getInstance();
        final VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + h, 0).tex(u0, v1).endVertex();
        buffer.pos(x + w, y + h, 0).tex(u1, v1).endVertex();
        buffer.pos(x + w, y, 0).tex(u1, v0).endVertex();
        buffer.pos(x, y, 0).tex(u0, v0).endVertex();
        tessellator.draw();
    }

    /**
     * Draw a full one-by-one quad with the specified texture coordinates.
     *
     * @param u0 lower u texture coordinate.
     * @param v0 lower v texture coordinate.
     * @param u1 upper u texture coordinate.
     * @param v1 upper v texture coordinate.
     */
    @SideOnly(Side.CLIENT)
    public static void drawQuad(final float u0, final float v0, final float u1, final float v1) {
        drawQuad(0, 0, 1, 1, u0, v0, u1, v1);
    }

    /**
     * Draw a full one-by-one quad.
     */
    @SideOnly(Side.CLIENT)
    public static void drawQuad() {
        drawQuad(0, 0, 1, 1);
    }

    // --------------------------------------------------------------------- //

    private RenderUtil() {
    }
}
