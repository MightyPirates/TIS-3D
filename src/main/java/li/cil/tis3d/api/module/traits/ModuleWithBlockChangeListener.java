package li.cil.tis3d.api.module.traits;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.module.Module;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Modules implementing this interface will be notified when the block adjacent
 * to the module's {@link Casing} changes.
 * <p>
 * Specifically, this is called from the {@link Casing}'s
 * {@link Block#neighborChanged(BlockState, Level, BlockPos, Block, BlockPos, boolean)} method.
 */
@SuppressWarnings("deprecation")
public interface ModuleWithBlockChangeListener extends Module {
    /**
     * Called when a block adjacent to the hosting {@link Casing} changes.
     *
     * @param neighborPos      the position of the block that changed.
     * @param isModuleNeighbor whether the block that changed is the one in front of the module, for convenience.
     */
    void onNeighborBlockChange(final BlockPos neighborPos, final boolean isModuleNeighbor);
}
