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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
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

    @SideOnly(Side.CLIENT)
    protected static void bindTexture(final ResourceLocation location) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
    }

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

    @SideOnly(Side.CLIENT)
    protected static void drawQuad(final float u0, final float v0, final float u1, final float v1) {
        drawQuad(0, 0, 1, 1, u0, v0, u1, v1);
    }

    @SideOnly(Side.CLIENT)
    protected static void drawQuad() {
        drawQuad(0, 0, 1, 1);
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

    @Override
    public void step() {
    }

    @Override
    public void onEnabled() {
    }

    @Override
    public void onDisabled() {
    }

    @Override
    public void onWriteComplete(final Port port) {
    }

    @Override
    public boolean onActivate(final EntityPlayer player, final float hitX, final float hitY, final float hitZ) {
        return false;
    }

    @Override
    public void onData(final NBTTagCompound nbt) {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
    }
}
