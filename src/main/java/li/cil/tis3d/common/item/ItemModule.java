package li.cil.tis3d.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Base item for all modules.
 */
public final class ItemModule extends Item {
    private boolean doesSneakBypassUse;

    public ItemModule setDoesSneakBypassUse(final boolean value) {
        this.doesSneakBypassUse = value;
        return this;
    }

    // --------------------------------------------------------------------- //
    // Item

    @Override
    public boolean doesSneakBypassUse(final World world, final int x, final int y, final int z, final EntityPlayer player) {
    //final ItemStack stack, 
        return doesSneakBypassUse;
    }
}
