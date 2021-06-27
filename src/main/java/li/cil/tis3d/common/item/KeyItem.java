package li.cil.tis3d.common.item;

import li.cil.tis3d.common.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nullable;

/**
 * Base item for all keys.
 */
public final class KeyItem extends ModItem {
    public KeyItem() {
        super(createProperties().stacksTo(1));
    }

    // --------------------------------------------------------------------- //
    // Item

    @Nullable
    @Override
    public CompoundNBT getShareTag(final ItemStack stack) {
        return null;
    }

    @Override
    public boolean doesSneakBypassUse(final ItemStack stack, final IWorldReader world, final BlockPos pos, final PlayerEntity player) {
        return world.getBlockState(pos).getBlock() == Blocks.CASING.get();
    }

    @Override
    public boolean isEnchantable(final ItemStack stack) {
        return false;
    }
}
