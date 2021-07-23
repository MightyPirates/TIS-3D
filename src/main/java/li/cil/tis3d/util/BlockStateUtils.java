package li.cil.tis3d.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public final class BlockStateUtils {
    @Nullable
    public static BlockState getBlockStateFromItemStack(final ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        final Block block = Block.byItem(stack.getItem());
        //noinspection ConstantConditions Block may be null because ItemBlock.getBlock can return null.
        if (block == null || block == Blocks.AIR) {
            return null;
        }

        return block.defaultBlockState();
    }

    // --------------------------------------------------------------------- //

    private BlockStateUtils() {
    }
}
