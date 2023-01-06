package li.cil.tis3d.api.prefab.module;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.api.util.TransformUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Base implementation of a module, taking care of the boilerplate code.
 */
public abstract class AbstractModule implements Module {
    // --------------------------------------------------------------------- //
    // Computed data

    private final Casing casing;
    private final Face face;
    private boolean isDisposed;

    protected AbstractModule(final Casing casing, final Face face) {
        this.casing = casing;
        this.face = face;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() {
        if (!isDisposed) {
            MainThreadDisposer.add(this);
        }
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
     * @param hitResult the current hit result.
     * @return <tt>true</tt> if the observer is looking at the module, <tt>false</tt> otherwise.
     */
    protected boolean isHitFace(@Nullable final HitResult hitResult) {
        if (!(hitResult instanceof final BlockHitResult blockHitResult)) {
            return false;
        }

        final BlockPos pos = blockHitResult.getBlockPos();
        return Objects.equals(getCasing().getPosition(), pos) &&
            blockHitResult.getDirection() == Face.toDirection(getFace());
    }

    /**
     * Utility method for determining the hit coordinate on the module's face the player is
     * looking at. This will return {@code null} if the player is not currently looking
     * at the module.
     * <p>
     * Note that this will return the unadjusted X, Y and Z components. To transform this
     * coordinate to a UV coordinate mapped to the module's face, pass this into
     * {@link #hitToUV}. Note that this method is overridden in {@link AbstractModuleWithRotation}
     * to also take into account the module's rotation.
     *
     * @param hitResult the current hit result.
     * @return the UV coordinate the observer is looking at as the X and Y components.
     */
    @Nullable
    protected Vec3 getLocalHitPosition(@Nullable final HitResult hitResult) {
        if (!(hitResult instanceof final BlockHitResult blockHitResult)) {
            return null;
        }

        final BlockPos pos = blockHitResult.getBlockPos();
        if (!Objects.equals(getCasing().getPosition(), pos)) {
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
     * @see #getLocalHitPosition(HitResult)
     * @see Module#use(Player, InteractionHand, Vec3)
     */
    protected Vec3 hitToUV(final Vec3 hitPos) {
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
        final Level level = getCasing().getCasingLevel();
        final BlockPos neighborPos = getCasing().getPosition().relative(Face.toDirection(getFace()));
        if (!level.isLoaded(neighborPos)) {
            // If the neighbor isn't loaded, we can assume we're also not visible on that side.
            return false;
        }

        // Otherwise check if the neighboring block blocks visibility to our face.
        final BlockState neighborState = level.getBlockState(neighborPos);
        return !neighborState.isSolidRender(level, neighborPos);
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
        isDisposed = true;
    }

    @Override
    public void onBeforeWriteComplete(final Port port) {
    }

    @Override
    public void onWriteComplete(final Port port) {
    }

    @Override
    public boolean use(final Player player, final InteractionHand hand, final Vec3 hit) {
        return false;
    }

    @Override
    public void onData(final CompoundTag data) {
    }

    @Override
    public void onData(final ByteBuf data) {
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(final RenderContext context) {
    }

    @Override
    public void load(final CompoundTag tag) {
    }

    @Override
    public void save(final CompoundTag tag) {
    }

    // --------------------------------------------------------------------- //

    /**
     * Used to make absolutely sure we call onDisposed on modules. There are some cases
     * where we cannot guarantee that this is called regularly, e.g. on level unload.
     * We want to make sure to call it though, because some modules may own unmanaged
     * resources such as GPU memory.
     */
    @Environment(EnvType.CLIENT)
    @ApiStatus.Internal
    public static final class MainThreadDisposer {
        private static final ConcurrentLinkedQueue<Module> modules = new ConcurrentLinkedQueue<>();

        static void add(final Module module) {
            modules.add(module);
        }

        public static void disposeModules() {
            Module module;
            while ((module = modules.poll()) != null) {
                module.onDisposed();
            }
        }
    }
}
