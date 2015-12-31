package li.cil.tis3d.api.prefab.module;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
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
    // Communication utility

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
            final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
            receivingPipe.cancelRead();
        }
    }

    // --------------------------------------------------------------------- //
    // Rendering utility

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

    /**
     * Utility method for determining the hit coordinate on the module's face the player is
     * looking at. This will return <tt>null</tt> if the player is not currently looking
     * at the module.
     * <p>
     * Note that this will return the unadjusted X, Y and Z components. To transform this
     * coordinate to a UV coordinate mapped to the module's face, pass this into
     * {@link #hitToUV}. Note that this method is overridden in {@link AbstractModuleRotatable}
     * to also take into account the module's rotation.
     *
     * @return the UV coordinate the player is looking at as the X and Y components.
     */
    @SideOnly(Side.CLIENT)
    protected Vec3 getPlayerLookAt() {
        final MovingObjectPosition hit = Minecraft.getMinecraft().objectMouseOver;
        if (hit != null &&
                hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
                getCasing().getPosition().equals(hit.getBlockPos()) &&
                hit.sideHit == Face.toEnumFacing(getFace())) {
            return new Vec3(hit.hitVec.xCoord - hit.getBlockPos().getX(),
                    hit.hitVec.yCoord - hit.getBlockPos().getY(),
                    hit.hitVec.zCoord - hit.getBlockPos().getZ());
        } else {
            return null;
        }
    }

    // --------------------------------------------------------------------- //
    // General utility

    /**
     * Project a hit position on the surface of a casing to a UV coordinate on
     * the face of this module.
     * <p>
     * Note that this is also overridden in {@link AbstractModuleRotatable} to
     * take into account the module's rotation.
     *
     * @param hitPos the hit position to project.
     * @return the projected UV coordinate, with the Z component being 0.
     * @see #getPlayerLookAt()
     * @see #onActivate(EntityPlayer, float, float, float)
     */
    protected Vec3 hitToUV(final Vec3 hitPos) {
        switch (getFace()) {
            case Y_NEG:
                return new Vec3(1 - hitPos.xCoord, hitPos.zCoord, 0);
            case Y_POS:
                return new Vec3(1 - hitPos.xCoord, 1 - hitPos.zCoord, 0);
            case Z_NEG:
                return new Vec3(1 - hitPos.xCoord, 1 - hitPos.yCoord, 0);
            case Z_POS:
                return new Vec3(hitPos.xCoord, 1 - hitPos.yCoord, 0);
            case X_NEG:
                return new Vec3(hitPos.zCoord, 1 - hitPos.yCoord, 0);
            case X_POS:
                return new Vec3(1 - hitPos.zCoord, 1 - hitPos.yCoord, 0);
        }
        return null;
    }

    /**
     * Convenience check for determining whether a module is actually visible.
     * <p>
     * This can allow for some optimizations, such as sending state updates
     * much more or infrequently (or not at all) while invisible. If rendering
     * a module's overlay is exceptionally complex,
     *
     * @return whether the module is currently visible.
     */
    protected boolean isVisible() {
        final World world = getCasing().getCasingWorld();
        final BlockPos neighborPos = getCasing().getPosition().offset(Face.toEnumFacing(getFace()));
        if (!world.isBlockLoaded(neighborPos)) {
            // If the neighbor isn't loaded, we can assume we're also not visible on that side.
            return false;
        }

        final Chunk chunk = world.getChunkFromBlockCoords(neighborPos);
        if (chunk == null || chunk.isEmpty()) {
            // If the neighbor chunk is empty, we must assume we're visible.
            return true;
        }

        // Otherwise check if the neighboring block blocks visibility to our face.
        final Block neighborBlock = world.getBlockState(neighborPos).getBlock();
        return !neighborBlock.doesSideBlockRendering(world, neighborPos, Face.toEnumFacing(getFace().getOpposite()));
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
    public void onInstalled(final ItemStack stack) {
    }

    @Override
    public void onUninstalled(final ItemStack stack) {
    }

    @Override
    public void onEnabled() {
    }

    @Override
    public void onDisabled() {
    }

    @Override
    public void onDisposed() {
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

    @Override
    public void onData(final ByteBuf data) {
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
