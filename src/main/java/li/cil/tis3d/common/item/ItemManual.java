package li.cil.tis3d.common.item;

import li.cil.tis3d.api.ManualAPI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * The manual!
 */
public final class ItemManual extends ItemBook {
    @Override
    public boolean isItemTool(final ItemStack stack) {
        return false;
    }

    @Override
    public int getItemEnchantability() {
        return 0;
    }

    @Override
    public boolean onItemUse(final ItemStack stack, final EntityPlayer playerIn, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final String path = ManualAPI.pathFor(world, pos);
        if (path != null) {
            if (world.isRemote) {
                ManualAPI.openFor(playerIn);
                ManualAPI.reset();
                ManualAPI.navigate(path);
            }
            return true;
        }
        return super.onItemUse(stack, playerIn, world, pos, side, hitX, hitY, hitZ);
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack stack, final World world, final EntityPlayer playerIn) {
        if (world.isRemote) {
            if (playerIn.isSneaking()) {
                ManualAPI.reset();
            }
            ManualAPI.openFor(playerIn);
        }
        return super.onItemRightClick(stack, world, playerIn);
    }
}
