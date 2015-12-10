package li.cil.tis3d.common.item;

import li.cil.tis3d.api.ManualAPI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

/**
 * The manual!
 */
public final class ItemManual extends ItemBook {
    public static final String TOOLTIP_MANUAL = "tis3d.tooltip.manual";

    @Override
    public boolean isItemTool(final ItemStack stack) {
        return false;
    }

    @Override
    public int getItemEnchantability() {
        return 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(final ItemStack stack, final EntityPlayer player, final List tooltip, final boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(StatCollector.translateToLocal(TOOLTIP_MANUAL));
    }

    @Override
    public boolean onItemUse(final ItemStack stack, final EntityPlayer player, final World world, final int x, final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ) {
        final String path = ManualAPI.pathFor(world, x, y, z);
        if (path != null) {
            if (world.isRemote) {
                ManualAPI.openFor(player);
                ManualAPI.reset();
                ManualAPI.navigate(path);
            }
            return true;
        }
        return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
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
