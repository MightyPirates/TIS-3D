package li.cil.tis3d.api.util;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.Matrix3f;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/**
 * Utility class for rendering related operations.
 */
public final class RenderUtil {
    /**
     * Bind the texture at the specified location to be used for quad rendering.
     *
     * @param location the location of the texture to bind.
     */
    @Environment(EnvType.CLIENT)
    public static void bindTexture(final Identifier location) {
        MinecraftClient.getInstance().getTextureManager().bindTexture(location);
    }

    /**
     * Get the texture atlas sprite for the specified texture loaded into the
     * block texture map.
     *
     * @param location the location of the texture to get the sprite for.
     * @return the sprite of the texture in the block atlas; <code>missingno</code> if not found.
     */
    @Environment(EnvType.CLIENT)
    public static Sprite getSprite(final Identifier location) {
        final MinecraftClient mc = MinecraftClient.getInstance();
        return mc.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(location);
    }

    /**
     * Draw a full one-by-one, untextured quad.
     *
     * @param x the x position of the quad.
     * @param y the y position of the quad.
     * @param w the width of the quad.
     * @param h the height of the quad.
     */
    @Environment(EnvType.CLIENT)
    public static void drawUntexturedQuad(final float x, final float y, final float w, final float h) {
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION);
        buffer.vertex(x, y + h, 0).next();
        buffer.vertex(x + w, y + h, 0).next();
        buffer.vertex(x + w, y, 0).next();
        buffer.vertex(x, y, 0).next();
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
    @Environment(EnvType.CLIENT)
    public static void drawQuad(final float x, final float y, final float w, final float h, final float u0, final float v0, final float u1, final float v1) {
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(x, y + h, 0).texture(u0, v1).next();
        buffer.vertex(x + w, y + h, 0).texture(u1, v1).next();
        buffer.vertex(x + w, y, 0).texture(u1, v0).next();
        buffer.vertex(x, y, 0).texture(u0, v0).next();
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
    @Environment(EnvType.CLIENT)
    public static void drawQuad(final float u0, final float v0, final float u1, final float v1) {
        drawQuad(0, 0, 1, 1, u0, v0, u1, v1);
    }

    /**
     * Draw a full one-by-one quad.
     */
    @Environment(EnvType.CLIENT)
    public static void drawQuad() {
        drawQuad(0, 0, 1, 1);
    }

    /**
     * Draw an arbitrarily sized quad with the specified texture coordinates and texture.
     * <p>
     * The UV coordinates are relative to the sprite.
     *
     * @param sprite the sprite to render.
     * @param x      the x position of the quad.
     * @param y      the y position of the quad.
     * @param w      the width of the quad.
     * @param h      the height of the quad.
     * @param u0     lower u texture coordinate.
     * @param v0     lower v texture coordinate.
     * @param u1     upper u texture coordinate.
     * @param v1     upper v texture coordinate.
     */
    @Environment(EnvType.CLIENT)
    public static void drawQuad(final Sprite sprite, final float x, final float y, final float w, final float h, final float u0, final float v0, final float u1, final float v1) {
        drawQuad(x, y, w, h, sprite.getFrameU(u0 * 16), sprite.getFrameV(v0 * 16), sprite.getFrameU(u1 * 16), sprite.getFrameV(v1 * 16));
    }

    /**
     * Draw a full one-by-one quad with the specified texture coordinates and sprite texture.
     * <p>
     * The UV coordinates are relative to the sprite.
     *
     * @param sprite the sprite to render.
     * @param u0     lower u texture coordinate.
     * @param v0     lower v texture coordinate.
     * @param u1     upper u texture coordinate.
     * @param v1     upper v texture coordinate.
     */
    @Environment(EnvType.CLIENT)
    public static void drawQuad(final Sprite sprite, final float u0, final float v0, final float u1, final float v1) {
        drawQuad(sprite, 0, 0, 1, 1, u0, v0, u1, v1);
    }

    /**
     * Draw a full one-by-one quad with the specified sprite texture.
     *
     * @param sprite the sprite to render.
     */
    @Environment(EnvType.CLIENT)
    public static void drawQuad(final Sprite sprite) {
        drawQuad(sprite, 0, 0, 1, 1);
    }

    @Environment(EnvType.CLIENT)
    public static void drawQuad(final MatrixStack.Entry matrices, final VertexConsumer vc,
                                final float x, final float y, final float w, final float h,
                                final float u0, final float v0, final float u1, final float v1,
                                final int l, final int ol) {

        final Matrix4f modMat = matrices.getModel();
        final Matrix3f normMat = matrices.getNormal();
        final Vector3f normDir = new Vector3f(0, 0, -1);
        final int c = 0xFF;

        vc.vertex(modMat, x, y + h, 0).color(c, c, c, c).texture(u0, v1)
          .overlay(ol).light(l)
          .normal(normMat, normDir.getX(), normDir.getY(), normDir.getZ())
          .next();

        vc.vertex(modMat, x + w, y + h, 0).color(c, c, c, c).texture(u1, v1)
          .overlay(ol).light(l)
          .normal(normMat, normDir.getX(), normDir.getY(), normDir.getZ())
          .next();

        vc.vertex(modMat, x + w, y, 0).color(c, c, c, c).texture(u1, v0)
          .overlay(ol).light(l)
          .normal(normMat, normDir.getX(), normDir.getY(), normDir.getZ())
          .next();

        vc.vertex(modMat, x, y, 0).color(c, c, c, c).texture(u0, v0)
          .overlay(ol).light(l)
          .normal(normMat, normDir.getX(), normDir.getY(), normDir.getZ())
          .next();
    }

    @Environment(EnvType.CLIENT)
    public static void drawQuad(final Sprite sprite, final MatrixStack.Entry matrices,
                                final VertexConsumer vc, final int light, final int overlay) {
        drawQuad(matrices, vc, 0, 0, 1, 1,
                 sprite.getFrameU(0 * 16), sprite.getFrameV(0 * 16),
                 sprite.getFrameU(1 * 16), sprite.getFrameV(1 * 16),
                 light, overlay);
    }

    /**
     * Configure the light map so that whatever is rendered next is rendered at
     * full brightness, regardless of environment brightness. Useful for rendering
     * overlays that should be emissive to also be visible in the dark.
     */
    @Environment(EnvType.CLIENT)
    public static void ignoreLighting() {
        RenderSystem.glMultiTexCoord2f(GL13.GL_TEXTURE1, 240, 240);
    }

    // --------------------------------------------------------------------- //

    private RenderUtil() {
    }
}
