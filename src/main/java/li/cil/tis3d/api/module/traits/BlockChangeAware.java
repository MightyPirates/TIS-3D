package li.cil.tis3d.api.module.traits;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Modules implementing this interface will be notified when the block adjacent
 * to the module's {@link Casing} changes.
 * <p>
 * Specifically, this is called from the {@link Casing}'s
 * {@link Block#onNeighborBlockChange(World, BlockPos, IBlockState, Block)} method.
 */
public interface BlockChangeAware extends Module {
    /**
     * Called when a block adjacent to the hosting {@link Casing} changes.
     *
     * @param neighborBlock the block that changed.
     */
    void onNeighborBlockChange(final Block neighborBlock);
}
