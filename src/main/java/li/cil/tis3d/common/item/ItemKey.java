package li.cil.tis3d.common.item;

import li.cil.tis3d.api.machine.Casing;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nullable;

/**
 * Base item for all keys.
 */
public final class ItemKey extends ModItem {
    public ItemKey() {
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
        return world.getBlockEntity(pos) instanceof Casing;
    }

    @Override
    public boolean isEnchantable(final ItemStack stack) {
        return false;
    }
}
