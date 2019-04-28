package li.cil.tis3d.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public final class BlockStateUtils {
    @SuppressWarnings("deprecation")
    @Nullable
    public static IBlockState getBlockStateFromItemStack(@Nullable final ItemStack stack) {
        if (stack == null || stack.stackSize < 1) {
            return null;
        }

        final Block block = Block.getBlockFromItem(stack.getItem());
        //noinspection ConstantConditions Block may be null because ItemBlock.getBlock can return null.
        if (block == null || block == Blocks.AIR) {
            return null;
        }

        try {
            if (stack.getMaxDamage() > 0) {
                return block.getStateFromMeta(0);
            } else {
                return block.getStateFromMeta(stack.getMetadata());
            }
        } catch (final Exception | LinkageError ignored) {
            return block.getDefaultState();
        }
    }

    // --------------------------------------------------------------------- //

    private BlockStateUtils() {
    }
}
