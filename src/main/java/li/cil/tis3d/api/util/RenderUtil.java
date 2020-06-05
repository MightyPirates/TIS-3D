package li.cil.tis3d.api.util;

import com.mojang.blaze3d.systems.RenderSystem;
import li.cil.tis3d.client.init.Textures;
import li.cil.tis3d.util.ColorUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/**
 * Utility class for rendering related operations.
 */
@Environment(EnvType.CLIENT)
public final class RenderUtil {
    /**
     * Passing this value as the {@code light} parameter to any of the functions
     * in this class makes the rendered quad ignore daylight.
     */
    public static final int maxLight = LightmapTextureManager.pack(0xF, 0xF);

    private static Sprite whiteSprite;

    /**
     * Instead of defining a custom RenderLayer for colored quads, we can just
     * reuse the common textured layers using a clear white dummy texture.
     * Colored quads being drawn in the same batch as textured ones is an
     * additional benefit of this approach.
     *
     * @return a cached Sprite instance pointing at the dummy texture
     */
    private static Sprite getWhiteSprite() {
        if (whiteSprite == null) {
            whiteSprite = getSprite(Textures.LOCATION_OVERLAY_UTIL_WHITE);
        }

        return whiteSprite;
    }

    /**
     * Bind the texture at the specified location to be used for quad rendering.
     *
     * @param location the location of the texture to bind.
     */
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
     * Draw a full one-by-one, textureless, colored quad.
     *
     * @param matrices the model/normal matrix pair to apply
     * @param vc       a VertexConsumer accepting all standard elements.
     * @param x        the x position of the quad.
     * @param y        the y position of the quad.
     * @param w        the width of the quad.
     * @param h        the height of the quad.
     * @param argb     the color in unsigned ARGB format.
     * @param light    the light value.
     * @param overlay  the overlay value.
     */
    public static void drawColorQuad(final MatrixStack.Entry matrices, final VertexConsumer vc,
                                     final float x, final float y, final float w, final float h,
                                     final int argb, final int light, final int overlay) {
        drawColorQuad(matrices, vc, x, y, w, h,
                      ColorUtils.getAlphaU8(argb),
                      ColorUtils.getRedU8(argb),
                      ColorUtils.getGreenU8(argb),
                      ColorUtils.getBlueU8(argb),
                      light, overlay);
    }

    /**
     * Draw a full one-by-one, textureless, colored quad.
     * <p>
     * Color components are in the [0, 255] range.
     *
     * @param matrices the model/normal matrix pair to apply
     * @param vc       a VertexConsumer accepting all standard elements.
     * @param x        the x position of the quad.
     * @param y        the y position of the quad.
     * @param w        the width of the quad.
     * @param h        the height of the quad.
     * @param a        the alpha color component.
     * @param r        the red color component.
     * @param g        the green color component.
     * @param b        the blue color component.
     * @param l        the light value.
     * @param ol       the overlay value.
     */
    public static void drawColorQuad(final MatrixStack.Entry matrices, final VertexConsumer vc,
                                     final float x, final float y, final float w, final float h,
                                     final int a, final int r, final int g, final int b,
                                     final int l, final int ol) {
        final Sprite white = getWhiteSprite();

        drawQuad(matrices, vc, x, y, w, h,
                 white.getFrameU(0 * 16), white.getFrameV(0 * 16),
                 white.getFrameU(1 * 16), white.getFrameV(1 * 16),
                 a, r, g, b, l, ol);
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
    public static void drawQuad(final float u0, final float v0, final float u1, final float v1) {
        drawQuad(0, 0, 1, 1, u0, v0, u1, v1);
    }

    /**
     * Draw a full one-by-one quad.
     */
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
    public static void drawQuad(final Sprite sprite, final float x, final float y, final float w, final float h, final float u0, final float v0, final float u1, final float v1) {
        drawQuad(x, y, w, h, sprite.getFrameU(u0 * 16), sprite.getFrameV(v0 * 16), sprite.getFrameU(u1 * 16), sprite
            .getFrameV(v1 * 16));
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
    public static void drawQuad(final Sprite sprite, final float u0, final float v0, final float u1, final float v1) {
        drawQuad(sprite, 0, 0, 1, 1, u0, v0, u1, v1);
    }

    /**
     * Draw a full one-by-one quad with the specified sprite texture.
     *
     * @param sprite the sprite to render.
     */
    public static void drawQuad(final Sprite sprite) {
        drawQuad(sprite, 0, 0, 1, 1);
    }

    /**
     * Draw an arbitrarily sized colored quad with the specified texture coordinates.
     * <p>
     * Color components are in the [0, 255] range.
     *
     * @param matrices the transformation.
     * @param vc       a VertexConsumer accepting all standard elements.
     * @param x        the x position of the quad.
     * @param y        the y position of the quad.
     * @param w        the width of the quad.
     * @param h        the height of the quad.
     * @param u0       lower u texture coordinate.
     * @param v0       lower v texture coordinate.
     * @param u1       upper u texture coordinate.
     * @param v1       upper v texture coordinate.
     * @param a        the alpha color component.
     * @param r        the red color component.
     * @param g        the green color component.
     * @param b        the blue color component.
     * @param light    the light value.
     * @param overlay  the overlay value.
     */
    public static void drawQuad(final MatrixStack.Entry matrices, final VertexConsumer vc,
                                final float x, final float y, final float w, final float h,
                                final float u0, final float v0, final float u1, final float v1,
                                final int a, final int r, final int g, final int b,
                                final int light, final int overlay) {

        final Matrix4f modMat = matrices.getModel();
        final Matrix3f normMat = matrices.getNormal();
        final Vector3f normDir = new Vector3f(0, 0, -1);

        vc.vertex(modMat, x, y + h, 0).color(r, g, b, a).texture(u0, v1)
          .overlay(overlay).light(light)
          .normal(normMat, normDir.getX(), normDir.getY(), normDir.getZ())
          .next();

        vc.vertex(modMat, x + w, y + h, 0).color(r, g, b, a).texture(u1, v1)
          .overlay(overlay).light(light)
          .normal(normMat, normDir.getX(), normDir.getY(), normDir.getZ())
          .next();

        vc.vertex(modMat, x + w, y, 0).color(r, g, b, a).texture(u1, v0)
          .overlay(overlay).light(light)
          .normal(normMat, normDir.getX(), normDir.getY(), normDir.getZ())
          .next();

        vc.vertex(modMat, x, y, 0).color(r, g, b, a).texture(u0, v0)
          .overlay(overlay).light(light)
          .normal(normMat, normDir.getX(), normDir.getY(), normDir.getZ())
          .next();
    }

    /**
     * Draw an arbitrarily sized quad with the specified texture coordinates and texture.
     *
     * @param matrices the transformation.
     * @param vc       a VertexConsumer accepting all standard elements.
     * @param x        the x position of the quad.
     * @param y        the y position of the quad.
     * @param w        the width of the quad.
     * @param h        the height of the quad.
     * @param u0       lower u texture coordinate.
     * @param v0       lower v texture coordinate.
     * @param u1       upper u texture coordinate.
     * @param v1       upper v texture coordinate.
     * @param light    the light value.
     * @param overlay  the overlay value.
     */
    public static void drawQuad(final MatrixStack.Entry matrices, final VertexConsumer vc,
                                final float x, final float y, final float w, final float h,
                                final float u0, final float v0, final float u1, final float v1,
                                final int light, final int overlay) {
        drawQuad(matrices, vc, x, y, w, h, u0, v0, u1, v1, 0xFF, 0xFF, 0xFF, 0xFF, light, overlay);
    }

    /**
     * Draw an arbitrarily sized colored quad with the specified texture coordinates and texture.
     *
     * @param matrices the transformation.
     * @param vc       a VertexConsumer accepting all standard elements.
     * @param x        the x position of the quad.
     * @param y        the y position of the quad.
     * @param w        the width of the quad.
     * @param h        the height of the quad.
     * @param u0       lower u texture coordinate.
     * @param v0       lower v texture coordinate.
     * @param u1       upper u texture coordinate.
     * @param v1       upper v texture coordinate.
     * @param argb     the color in unsigned ARGB format.
     * @param light    the light value.
     * @param overlay  the overlay value.
     */
    public static void drawQuad(final MatrixStack.Entry matrices, final VertexConsumer vc,
                                final float x, final float y, final float w, final float h,
                                final float u0, final float v0, final float u1, final float v1,
                                final int argb, final int light, final int overlay) {

        drawQuad(matrices, vc, x, y, w, h, u0, v0, u1, v1,
                 ColorUtils.getAlphaU8(argb),
                 ColorUtils.getRedU8(argb),
                 ColorUtils.getGreenU8(argb),
                 ColorUtils.getBlueU8(argb),
                 light, overlay);
    }

    /**
     * Draw an arbitrarily sized quad with the specified texture coordinates and texture.
     * <p>
     * The UV coordinates are relative to the sprite.
     *
     * @param sprite   the sprite to render.
     * @param matrices the transformation.
     * @param vc       a VertexConsumer accepting all standard elements.
     * @param x        the x position of the quad.
     * @param y        the y position of the quad.
     * @param w        the width of the quad.
     * @param h        the height of the quad.
     * @param u0       lower u texture coordinate.
     * @param v0       lower v texture coordinate.
     * @param u1       upper u texture coordinate.
     * @param v1       upper v texture coordinate.
     * @param light    the light value.
     * @param overlay  the overlay value.
     */
    public static void drawQuad(final Sprite sprite, final MatrixStack.Entry matrices, final VertexConsumer vc,
                                final float x, final float y, final float w, final float h,
                                final float u0, final float v0, final float u1, final float v1,
                                final int light, final int overlay) {
        drawQuad(matrices, vc,
                 x, y, w, h,
                 sprite.getFrameU(u0 * 16), sprite.getFrameV(v0 * 16),
                 sprite.getFrameU(u1 * 16), sprite.getFrameV(v1 * 16),
                 light, overlay);
    }

    /**
     * Draw a full one-by-one colored quad with the specified sprite texture.
     * <p>
     * Color components are in the [0, 255] range.
     *
     * @param sprite   the sprite to render.
     * @param matrices the transformation.
     * @param vc       a VertexConsumer accepting all standard elements.
     * @param a        the alpha color component.
     * @param r        the red color component.
     * @param g        the green color component.
     * @param b        the blue color component.
     * @param light    the light value.
     * @param overlay  the overlay value.
     */
    public static void drawQuad(final Sprite sprite, final MatrixStack.Entry matrices,
                                final VertexConsumer vc,
                                final int a, final int r, final int g, final int b,
                                final int light, final int overlay) {
        drawQuad(matrices, vc,
                 0, 0, 1, 1,
                 sprite.getFrameU(0 * 16), sprite.getFrameV(0 * 16),
                 sprite.getFrameU(1 * 16), sprite.getFrameV(1 * 16),
                 a, r, g, b,
                 light, overlay);
    }

    /**
     * Draw a full one-by-one quad with the specified sprite texture.
     *
     * @param sprite   the sprite to render.
     * @param matrices the transformation.
     * @param vc       a VertexConsumer accepting all standard elements.
     * @param light    the light value.
     * @param overlay  the overlay value.
     */
    public static void drawQuad(final Sprite sprite, final MatrixStack.Entry matrices,
                                final VertexConsumer vc, final int light, final int overlay) {
        drawQuad(sprite, matrices, vc, 0, 0, 1, 1, 0, 0, 1, 1, light, overlay);
    }

    /**
     * Draw a full one-by-one quad.
     *
     * @param matrices the transformation.
     * @param vc       a VertexConsumer accepting all standard elements.
     * @param light    the light value.
     * @param overlay  the overlay value.
     */
    public static void drawQuad(final MatrixStack.Entry matrices,
                                final VertexConsumer vc, final int light, final int overlay) {
        drawQuad(matrices, vc, 0, 0, 1, 1, 0, 0, 1, 1, light, overlay);
    }

    /**
     * Configure the light map so that whatever is rendered next is rendered at
     * full brightness, regardless of environment brightness. Useful for rendering
     * overlays that should be emissive to also be visible in the dark.
     */
    public static void ignoreLighting() {
        RenderSystem.glMultiTexCoord2f(GL13.GL_TEXTURE1, 240, 240);
    }

    // --------------------------------------------------------------------- //

    private RenderUtil() {
    }
}
