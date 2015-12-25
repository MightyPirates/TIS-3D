package li.cil.tis3d.common.item;

import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.common.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * The manual!
 */
public final class ItemBookManual extends ItemBook {
    private static final String TOOLTIP_BOOK_MANUAL = "tis3d.tooltip.bookManual";

    // --------------------------------------------------------------------- //

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
        return Minecraft.getMinecraft().fontRendererObj;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final EntityPlayer player, final List<String> tooltip, final boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        final String info = StatCollector.translateToLocal(TOOLTIP_BOOK_MANUAL);
        tooltip.addAll(getFontRenderer(stack).listFormattedStringToWidth(info, Constants.MAX_TOOLTIP_WIDTH));
    }

    @Override
    public boolean onItemUse(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        return tryOpenManual(world, player, ManualAPI.pathFor(world, pos));
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
