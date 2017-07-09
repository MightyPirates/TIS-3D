package li.cil.tis3d.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;

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
    public boolean doesSneakBypassUse(final World world, final int x, final int y, final int z, final EntityPlayer player) {
        return doesSneakBypassUse;
    }

    @Override
    public boolean getShareTag() {
        return shareTag;
    }
}
