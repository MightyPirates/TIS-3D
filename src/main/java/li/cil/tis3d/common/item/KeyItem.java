package li.cil.tis3d.common.item;

import li.cil.tis3d.common.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

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
    public CompoundTag getShareTag(final ItemStack stack) {
        return null;
    }

    @Override
    public boolean doesSneakBypassUse(final ItemStack stack, final LevelReader world, final BlockPos pos, final Player player) {
        return world.getBlockState(pos).getBlock() == Blocks.CASING.get();
    }

    @Override
    public boolean isEnchantable(final ItemStack stack) {
        return false;
    }
}
