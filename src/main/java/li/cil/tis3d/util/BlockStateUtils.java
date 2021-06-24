package li.cil.tis3d.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public final class BlockStateUtils {
    @Nullable
    public static BlockState getBlockStateFromItemStack(final ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        final Block block = Block.getBlockFromItem(stack.getItem());
        //noinspection ConstantConditions Block may be null because ItemBlock.getBlock can return null.
        if (block == null || block == Blocks.AIR) {
            return null;
        }

        return block.getDefaultState();
    }

    // --------------------------------------------------------------------- //

    private BlockStateUtils() {
    }
}
