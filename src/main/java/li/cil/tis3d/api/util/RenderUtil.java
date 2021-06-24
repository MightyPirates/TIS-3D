package li.cil.tis3d.api.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import li.cil.tis3d.util.Color;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Utility class for rendering related operations.
 */
@OnlyIn(Dist.CLIENT)
public final class RenderUtil {
    /**
     * Draw an arbitrarily sized colored quad with the specified texture coordinates.
     * <p>
     * Color components are in the [0, 255] range.
     *
     * @param context the current render context.
     * @param buffer  the buffer collecting vertex information.
     * @param x       the x position of the quad.
     * @param y       the y position of the quad.
     * @param w       the width of the quad.
     * @param h       the height of the quad.
     * @param u0      lower u texture coordinate.
     * @param v0      lower v texture coordinate.
     * @param u1      upper u texture coordinate.
     * @param v1      upper v texture coordinate.
     * @param argb    the ARGB vertex color.
     */
    public static void drawQuad(final RenderContext context, final IVertexBuilder buffer,
                                final float x, final float y, final float w, final float h,
                                final float u0, final float v0, final float u1, final float v1,
                                final int argb) {
        final Matrix4f modMat = context.getMatrixStack().last().pose();
        final Matrix3f normMat = context.getMatrixStack().last().normal();
        final Vector3f normDir = new Vector3f(0, 0, -1);

        final int a = Color.getAlphaU8(argb);
        final int r = Color.getRedU8(argb);
        final int g = Color.getGreenU8(argb);
        final int b = Color.getBlueU8(argb);

        buffer.vertex(modMat, x, y + h, 0).color(r, g, b, a).uv(u0, v1)
            .overlayCoords(context.overlay).uv2(context.light)
            .normal(normMat, normDir.x(), normDir.y(), normDir.z())
            .endVertex();

        buffer.vertex(modMat, x + w, y + h, 0).color(r, g, b, a).uv(u1, v1)
            .overlayCoords(context.overlay).uv2(context.light)
            .normal(normMat, normDir.x(), normDir.y(), normDir.z())
            .endVertex();

        buffer.vertex(modMat, x + w, y, 0).color(r, g, b, a).uv(u1, v0)
            .overlayCoords(context.overlay).uv2(context.light)
            .normal(normMat, normDir.x(), normDir.y(), normDir.z())
            .endVertex();

        buffer.vertex(modMat, x, y, 0).color(r, g, b, a).uv(u0, v0)
            .overlayCoords(context.overlay).uv2(context.light)
            .normal(normMat, normDir.x(), normDir.y(), normDir.z())
            .endVertex();
    }

    /**
     * Draw an arbitrarily sized quad with the specified texture coordinates and texture.
     * <p>
     * The UV coordinates are relative to the sprite.
     *
     * @param context the current render context.
     * @param buffer  the buffer collecting vertex information.
     * @param sprite  the sprite to render.
     * @param x       the x position of the quad.
     * @param y       the y position of the quad.
     * @param w       the width of the quad.
     * @param h       the height of the quad.
     * @param u0      lower u texture coordinate.
     * @param v0      lower v texture coordinate.
     * @param u1      upper u texture coordinate.
     * @param v1      upper v texture coordinate.
     */
    public static void drawQuad(final RenderContext context, final IVertexBuilder buffer, final TextureAtlasSprite sprite,
                                final float x, final float y, final float w, final float h,
                                final float u0, final float v0, final float u1, final float v1) {
        drawQuad(context, buffer,
            x, y, w, h,
            sprite.getU(u0 * 16), sprite.getV(v0 * 16),
            sprite.getU(u1 * 16), sprite.getV(v1 * 16),
            Color.WHITE);
    }

    // --------------------------------------------------------------------- //

    private RenderUtil() {
    }
}
