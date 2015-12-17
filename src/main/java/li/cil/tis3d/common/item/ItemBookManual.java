package li.cil.tis3d.common.item;

import cpw.mods.fml.common.registry.GameRegistry;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.common.Constants;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

/**
 * The manual!
 */
public final class ItemBookManual extends ItemBook {
    private static final String TOOLTIP_BOOK_MANUAL = "tis3d.tooltip.bookManual";

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
        tooltip.add(StatCollector.translateToLocal(TOOLTIP_BOOK_MANUAL));
    }

    @Override
    public boolean onItemUse(final ItemStack stack, final EntityPlayer player, final World world, final int x, final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ) {
        return tryOpenManual(world, player, ManualAPI.pathFor(world, x, y, z));
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

    public static boolean isBookManual(final ItemStack stack) {
        return stack != null && stack.getItem() == GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_BOOK_MANUAL);
    }

    public static boolean tryOpenManual(final World world, final EntityPlayer player, final String path) {
        if (path == null) {
            return false;
        }

        if (world.isRemote) {
            ManualAPI.openFor(player);
            ManualAPI.reset();
            ManualAPI.navigate(path);
        }

        return true;
    }
}
