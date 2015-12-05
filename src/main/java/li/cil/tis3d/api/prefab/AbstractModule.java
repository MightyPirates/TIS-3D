package li.cil.tis3d.api.prefab;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Pipe;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Base implementation of a module, taking care of the boilerplate code.
 */
public abstract class AbstractModule implements Module {
    // --------------------------------------------------------------------- //
    // Computed data

    private final Casing casing;
    private final Face face;

    protected AbstractModule(final Casing casing, final Face face) {
        this.casing = casing;
        this.face = face;
    }

    // --------------------------------------------------------------------- //
    // Communication utility.

    /**
     * Cancel writing on all ports.
     */
    protected void cancelWrite() {
        for (final Port port : Port.VALUES) {
            final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
            sendingPipe.cancelWrite();
        }
    }

    /**
     * Cancel reading on all ports.
     */
    protected void cancelRead() {
        for (final Port port : Port.VALUES) {
            final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
            sendingPipe.cancelRead();
        }
    }

    // --------------------------------------------------------------------- //
    // Rendering utility.

    /**
     * Bind the texture at the specified location to be used for quad rendering.
     *
     * @param location the location of the texture to bind.
     */
    @SideOnly(Side.CLIENT)
    protected static void bindTexture(final ResourceLocation location) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
    }

    /**
     * Draw an arbitrarily sized quad with the specified texture coordinates.
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
    protected static void drawQuad(final float x, final float y, final float w, final float h, final float u0, final float v0, final float u1, final float v1) {
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(x, y + h, 0).tex(u0, v1).endVertex();
        worldRenderer.pos(x + w, y + h, 0).tex(u1, v1).endVertex();
        worldRenderer.pos(x + w, y, 0).tex(u1, v0).endVertex();
        worldRenderer.pos(x, y, 0).tex(u0, v0).endVertex();
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
    protected static void drawQuad(final float u0, final float v0, final float u1, final float v1) {
        drawQuad(0, 0, 1, 1, u0, v0, u1, v1);
    }

    /**
     * Draw a full one-by-one quad.
     */
    @SideOnly(Side.CLIENT)
    protected static void drawQuad() {
        drawQuad(0, 0, 1, 1);
    }

    /**
     * Utility method for determining whether the player is currently looking at this module.
     *
     * @return <tt>true</tt> if the player is looking at the module, <tt>false</tt> otherwise.
     */
    @SideOnly(Side.CLIENT)
    protected boolean isPlayerLookingAt() {
        final MovingObjectPosition hit = Minecraft.getMinecraft().objectMouseOver;
        return hit != null &&
                hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
                getCasing().getPosition().equals(hit.getBlockPos()) &&
                hit.sideHit == Face.toEnumFacing(getFace());
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public Casing getCasing() {
        return casing;
    }

    @Override
    public Face getFace() {
        return face;
    }
}
