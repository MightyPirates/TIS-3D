package li.cil.tis3d.common.item;

import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.common.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
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
        final String info = I18n.translateToLocal(TOOLTIP_BOOK_MANUAL);
        tooltip.addAll(getFontRenderer(stack).listFormattedStringToWidth(info, Constants.MAX_TOOLTIP_WIDTH));
    }

    @Override
    public EnumActionResult onItemUse(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        return tryOpenManual(world, player, ManualAPI.pathFor(world, pos)) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final ItemStack stack, final World world, final EntityPlayer player, final EnumHand hand) {
        if (world.isRemote) {
            if (player.isSneaking()) {
                ManualAPI.reset();
            }
            ManualAPI.openFor(player);
        }
        return super.onItemRightClick(stack, world, player, hand);
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
