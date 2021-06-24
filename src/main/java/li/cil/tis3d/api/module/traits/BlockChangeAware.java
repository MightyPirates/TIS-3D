package li.cil.tis3d.api.module.traits;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Modules implementing this interface will be notified when the block adjacent
 * to the module's {@link Casing} changes.
 * <p>
 * Specifically, this is called from the {@link Casing}'s
 * {@link Block#neighborChanged(BlockState, World, BlockPos, Block, BlockPos, boolean)} method.
 */
@SuppressWarnings("deprecation")
public interface BlockChangeAware extends Module {
    /**
     * Called when a block adjacent to the hosting {@link Casing} changes.
     *
     * @param neighborPos      the position of the block that changed.
     * @param isModuleNeighbor whether the block that changed is the one in front of the module, for convenience.
     */
    void onNeighborBlockChange(final BlockPos neighborPos, final boolean isModuleNeighbor);
}
