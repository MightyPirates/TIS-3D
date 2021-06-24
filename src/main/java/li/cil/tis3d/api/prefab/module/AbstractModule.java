package li.cil.tis3d.api.prefab.module;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.api.util.TransformUtil;
import li.cil.tis3d.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

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
     * @param dispatcher the render context.
     * @return <tt>true</tt> if the observer is looking at the module, <tt>false</tt> otherwise.
     */
    @OnlyIn(Dist.CLIENT)
    protected boolean isHitFace(@Nullable final RayTraceResult hitResult) {
        if (hitResult == null) {
            return false;
        }
        if (hitResult.getType() != RayTraceResult.Type.BLOCK) {
            return false;
        }

        final BlockRayTraceResult blockHitResult = (BlockRayTraceResult) hitResult;
        if (!getCasing().getPosition().equals(blockHitResult.getBlockPos())) {
            return false;
        }
        if (blockHitResult.getDirection() != Face.toDirection(getFace())) {
            return false;
        }

        return true;
    }

    /**
     * Utility method for determining the hit coordinate on the module's face the player is
     * looking at. This will return <tt>null</tt> if the player is not currently looking
     * at the module.
     * <p>
     * Note that this will return the unadjusted X, Y and Z components. To transform this
     * coordinate to a UV coordinate mapped to the module's face, pass this into
     * {@link #hitToUV}. Note that this method is overridden in {@link AbstractModuleWithRotation}
     * to also take into account the module's rotation.
     *
     * @param dispatcher the render context.
     * @return the UV coordinate the observer is looking at as the X and Y components.
     */
    @OnlyIn(Dist.CLIENT)
    @Nullable
    protected Vector3d getLocalHitPosition(@Nullable final RayTraceResult hitResult) {
        if (hitResult == null) {
            return null;
        }
        if (hitResult.getType() != RayTraceResult.Type.BLOCK) {
            return null;
        }

        final BlockRayTraceResult blockHitResult = (BlockRayTraceResult) hitResult;
        final BlockPos pos = blockHitResult.getBlockPos();
        if (!getCasing().getPosition().equals(pos)) {
            return null;
        }
        if (blockHitResult.getDirection() != Face.toDirection(getFace())) {
            return null;
        }

        return hitResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
    }

    // --------------------------------------------------------------------- //
    // General utility

    /**
     * Project a hit position on the surface of a casing to a UV coordinate on
     * the face of this module.
     * <p>
     * Note that this is also overridden in {@link AbstractModuleWithRotation} to
     * take into account the module's rotation.
     *
     * @param hitPos the hit position to project.
     * @return the projected UV coordinate, with the Z component being 0.
     * @see #getObserverLookAt(TileEntityRendererDispatcher)
     * @see Module#onActivate(PlayerEntity, Hand, Vector3d)
     */
    protected Vector3d hitToUV(final Vector3d hitPos) {
        return TransformUtil.hitToUV(getFace(), hitPos);
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
        final World world = getCasing().getCasingLevel();
        final BlockPos neighborPos = getCasing().getPosition().relative(Face.toDirection(getFace()));
        if (!WorldUtils.isBlockLoaded(world, neighborPos)) {
            // If the neighbor isn't loaded, we can assume we're also not visible on that side.
            return false;
        }

        final Chunk chunk = world.getChunkAt(neighborPos);
        if (chunk.isEmpty()) {
            // If the neighbor chunk is empty, we must assume we're visible.
            return true;
        }

        // Otherwise check if the neighboring block blocks visibility to our face.
        final BlockState neighborState = world.getBlockState(neighborPos);
        return !neighborState.canOcclude();
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
    public void onBeforeWriteComplete(final Port port) {
    }

    @Override
    public void onWriteComplete(final Port port) {
    }

    @Override
    public boolean onActivate(final PlayerEntity player, final Hand hand, final Vector3d hit) {
        return false;
    }

    @Override
    public void onData(final CompoundNBT nbt) {
    }

    @Override
    public void onData(final ByteBuf data) {
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(final RenderContext context) {
    }

    @Override
    public void readFromNBT(final CompoundNBT nbt) {
    }

    @Override
    public void writeToNBT(final CompoundNBT nbt) {
    }
}
