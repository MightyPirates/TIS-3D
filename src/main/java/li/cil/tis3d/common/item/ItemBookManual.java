package li.cil.tis3d.common.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.common.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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

    // --------------------------------------------------------------------- //
    // Item

    @SideOnly(Side.CLIENT)
    @Override
    public FontRenderer getFontRenderer(final ItemStack stack) {
        return Minecraft.getMinecraft().fontRenderer;
    }

    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final EntityPlayer player, final List tooltip, final boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        final String info = StatCollector.translateToLocal(Constants.TOOLTIP_BOOK_MANUAL);
        tooltip.addAll(getFontRenderer(stack).listFormattedStringToWidth(info, Constants.MAX_TOOLTIP_WIDTH));
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

    // --------------------------------------------------------------------- //
    // ItemBook

    @Override
    public boolean isItemTool(final ItemStack stack) {
        return false;
    }

    @Override
    public int getItemEnchantability() {
        return 0;
    }
}
