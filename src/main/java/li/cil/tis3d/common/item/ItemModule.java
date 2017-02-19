package li.cil.tis3d.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Base item for all modules.
 */
public final class ItemModule extends Item {
    private boolean doesSneakBypassUse;
    private boolean shareTag;

    public ItemModule setDoesSneakBypassUse(final boolean value) {
        this.doesSneakBypassUse = value;
        return this;
    }

    public ItemModule setShareTag(final boolean value) {
        this.shareTag = value;
        return this;
    }

    // --------------------------------------------------------------------- //
    // Item

    @Override
    public boolean doesSneakBypassUse(final ItemStack stack, final IBlockAccess world, final BlockPos pos, final EntityPlayer player) {
        return doesSneakBypassUse;
    }

    @Override
    public boolean getShareTag() {
        return shareTag;
    }
}
