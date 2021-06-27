package li.cil.tis3d.api.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import li.cil.manual.api.render.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Wraps up render context and provides several convenience methods for {@link li.cil.tis3d.api.module.Module} rendering.
 */
@OnlyIn(Dist.CLIENT)
public interface RenderContext {
    /**
     * Gets the current renderer in use for the context in which this module is being rendered.
     *
     * @return the current renderer.
     */
    TileEntityRendererDispatcher getDispatcher();

    /**
     * The matrix stack in use for this context.
     * <p>
     * Note that this returns a reference, not a copy, so changing the stack influences how text and quads are rendered.
     *
     * @return the current matrix stack.
     */
    MatrixStack getMatrixStack();

    /**
     * Current partial ticks in the active frame, i.e. the fractional tick that has elapsed in rendering time but not
     * yet in simulation time.
     *
     * @return the current partial ticks.
     */
    float getPartialTicks();

    /**
     * The current render buffer in use for this context.
     * <p>
     * Used to obtain buffer builders for batched rendering.
     *
     * @return the current buffer.
     */
    IRenderTypeBuffer getBuffer();

    /**
     * Utility method to determine if the observer we are rendering for is close enough so that detailed
     * rendering should be provided.
     *
     * @param position the position for which to check if the observer is close enough.
     * @return {@code true} if details should be rendered, {@code false} otherwise.
     */
    boolean closeEnoughForDetails(BlockPos position);

    /**
     * Draws a character sequence using the specified font renderer in the current render context.
     *
     * @param fontRenderer the font renderer to use.
     * @param value        the string to draw.
     * @param argb         the color to tint the text with.
     */
    void drawString(FontRenderer fontRenderer, CharSequence value, int argb);

    /**
     * Draws a 1x1 textured quad using current lighting information.
     * <p>
     * The texture referenced by the specified location is expected to live in the block texture atlas.
     *
     * @param location the location of the texture to draw.
     */
    void drawAtlasQuadLit(ResourceLocation location);

    /**
     * Draws a 1x1 textured quad at maximum brightness.
     * <p>
     * The texture referenced by the specified location is expected to live in the block texture atlas.
     *
     * @param location the location of the texture to draw.
     */
    void drawAtlasQuadUnlit(final ResourceLocation location);

    /**
     * Draws a 1x1 textured quad at maximum brightness.
     * <p>
     * The texture referenced by the specified location is expected to live in the block texture atlas.
     *
     * @param location the location of the texture to draw.
     * @param argb     the color tint of the quad as an ARGB color.
     */
    default void drawAtlasQuadUnlit(final ResourceLocation location, final int argb) {
        drawAtlasQuadUnlit(location, 0, 0, 1, 1, 0, 0, 1, 1, argb);
    }

    /**
     * Draws a textured quad with the specified size at maximum brightness.
     * <p>
     * The texture referenced by the specified location is expected to live in the block texture atlas.
     * <p>
     * The texture coordinates are in texture-local space, so will typically be in the range of [0, 1].
     *
     * @param location the location of the texture to draw.
     * @param x        the x coordinate of the minimum corner of the quad.
     * @param y        the y coordinate of the minimum corner of the quad.
     * @param width    the width of the quad.
     * @param height   the height of the quad.
     * @param u0       the u component of the UV coordinate of the minimum corner of the quad.
     * @param v0       the v component of the UV coordinate of the minimum corner of the quad.
     * @param u1       the u component of the UV coordinate of the maximum corner of the quad.
     * @param v1       the v component of the UV coordinate of the maximum corner of the quad.
     * @param argb     the color tint of the quad as an ARGB color.
     */
    void drawAtlasQuadUnlit(ResourceLocation location, float x, float y, float width, float height,
                            float u0, float v0, float u1, float v1, int argb);

    /**
     * Draws an unlit quad at maximum brightness.
     *
     * @param x      the x coordinate of the minimum corner of the quad.
     * @param y      the y coordinate of the minimum corner of the quad.
     * @param width  the width of the quad.
     * @param height the height of the quad.
     * @param argb   the color tint of the quad as an ARGB color.
     */
    void drawQuadUnlit(float x, float y, float width, float height, int argb);

    /**
     * Draws a quad with the specified size.
     *
     * @param builder the buffer builder to emit the quad into.
     * @param x       the x coordinate of the minimum corner of the quad.
     * @param y       the y coordinate of the minimum corner of the quad.
     * @param width   the width of the quad.
     * @param height  the height of the quad.
     */
    void drawQuad(final IVertexBuilder builder, final float x, final float y, final float width, final float height);

    /**
     * Draws a quad with the specified size and the specified tint.
     *
     * @param builder the buffer builder to emit the quad into.
     * @param x       the x coordinate of the minimum corner of the quad.
     * @param y       the y coordinate of the minimum corner of the quad.
     * @param width   the width of the quad.
     * @param height  the height of the quad.
     * @param argb    the color tint of the quad as an ARGB color.
     */
    default void drawQuad(final IVertexBuilder builder, final float x, final float y,
                          final float width, final float height, final int argb) {
        drawQuad(builder, x, y, width, height, 0, 0, 1, 1, argb);
    }

    /**
     * Draws a textured quad.
     * <p>
     * The texture referenced by the specified location is expected to live in the block texture atlas.
     * <p>
     * The texture coordinates are in texture-local space, so will typically be in the range of [0, 1].
     *
     * @param builder the buffer builder to emit the quad into.
     * @param sprite  the texture to draw.
     * @param x       the x coordinate of the minimum corner of the quad.
     * @param y       the y coordinate of the minimum corner of the quad.
     * @param width   the width of the quad.
     * @param height  the height of the quad.
     * @param u0      the u component of the UV coordinate of the minimum corner of the quad.
     * @param v0      the v component of the UV coordinate of the minimum corner of the quad.
     * @param u1      the u component of the UV coordinate of the maximum corner of the quad.
     * @param v1      the v component of the UV coordinate of the maximum corner of the quad.
     * @param argb    the color tint of the quad as an ARGB color.
     */
    default void drawAtlasQuad(final IVertexBuilder builder, final TextureAtlasSprite sprite,
                               final float x, final float y, final float width, final float height,
                               final float u0, final float v0, final float u1, final float v1, final int argb) {
        final float atlasU0 = sprite.getU(u0 * 16);
        final float atlasV0 = sprite.getV(v0 * 16);
        final float atlasU1 = sprite.getU(u1 * 16);
        final float atlasV1 = sprite.getV(v1 * 16);
        drawQuad(builder, x, y, width, height, atlasU0, atlasV0, atlasU1, atlasV1, argb);
    }

    /**
     * Draws a quad with the specified size and tint.
     * <p>
     * The texture coordinates must be resolved to the underlying texture in GPU memory. This means
     * that for atlas textures they must be in atlas space, for regular textures they must be in
     * texture-local space.
     *
     * @param builder the buffer builder to emit the quad into.
     * @param x       the x coordinate of the minimum corner of the quad.
     * @param y       the y coordinate of the minimum corner of the quad.
     * @param width   the width of the quad.
     * @param height  the height of the quad.
     * @param u0      the u component of the UV coordinate of the minimum corner of the quad.
     * @param v0      the v component of the UV coordinate of the minimum corner of the quad.
     * @param u1      the u component of the UV coordinate of the maximum corner of the quad.
     * @param v1      the v component of the UV coordinate of the maximum corner of the quad.
     * @param argb    the color tint of the quad as an ARGB color.
     */
    void drawQuad(IVertexBuilder builder, float x, float y, float width, float height,
                  float u0, float v0, float u1, float v1, int argb);
}
