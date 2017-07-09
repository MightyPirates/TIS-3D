package li.cil.tis3d.api.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

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
     * Get the texture atlas sprite for the specified texture loaded into the
     * block texture map.
     *
     * @param location the location of the texture to get the sprite for.
     * @return the sprite of the texture in the block atlas; <code>null</code> if not found.
     */
    @SideOnly(Side.CLIENT)
    @Nullable
    public static TextureAtlasSprite getSprite(final ResourceLocation location) {
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
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
        final Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertex(x, y + h, 0);
        tessellator.addVertex(x + w, y + h, 0);
        tessellator.addVertex(x + w, y, 0);
        tessellator.addVertex(x, y, 0);
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
        final Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + h, 0, u0, v1);
        tessellator.addVertexWithUV(x + w, y + h, 0, u1, v1);
        tessellator.addVertexWithUV(x + w, y, 0, u1, v0);
        tessellator.addVertexWithUV(x, y, 0, u0, v0);
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
    @SideOnly(Side.CLIENT)
    public static void drawQuad(final TextureAtlasSprite sprite, final float x, final float y, final float w, final float h, final float u0, final float v0, final float u1, final float v1) {
        drawQuad(x, y, w, h, sprite.getInterpolatedU(u0 * 16), sprite.getInterpolatedV(v0 * 16), sprite.getInterpolatedU(u1 * 16), sprite.getInterpolatedV(v1 * 16));
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
    @SideOnly(Side.CLIENT)
    public static void drawQuad(final TextureAtlasSprite sprite, final float u0, final float v0, final float u1, final float v1) {
        drawQuad(sprite, 0, 0, 1, 1, u0, v0, u1, v1);
    }

    /**
     * Draw a full one-by-one quad with the specified sprite texture.
     *
     * @param sprite the sprite to render.
     */
    @SideOnly(Side.CLIENT)
    public static void drawQuad(final TextureAtlasSprite sprite) {
        drawQuad(sprite, 0, 0, 1, 1);
    }

    /**
     * Configure the light map so that whatever is rendered next is rendered at
     * full brightness, regardless of environment brightness. Useful for rendering
     * overlays that should be emissive to also be visible in the dark.
     */
    @SideOnly(Side.CLIENT)
    public static void ignoreLighting() {
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    }

    // --------------------------------------------------------------------- //

    private RenderUtil() {
    }
}
