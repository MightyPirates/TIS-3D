package li.cil.tis3d.common.item;

import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.common.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;



import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The manual!
 */
public final class ItemBookManual extends ItemBook {
	public ItemBookManual(Item.Builder builder) {
		super(builder);
	}

    public static boolean tryOpenManual(final World world, final EntityPlayer player, @Nullable final String path) {
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


    @Override
    public void addInformation(final ItemStack stack, @Nullable final World world, final List<ITextComponent> tooltip, final ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        final String info = I18n.format(Constants.TOOLTIP_BOOK_MANUAL);
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
	    tooltip.addAll(fontRenderer.listFormattedStringToWidth(info, li.cil.tis3d.common.Constants.MAX_TOOLTIP_WIDTH).stream().map(TextComponentString::new).collect(Collectors.toList()));
    }

    @Override
    public EnumActionResult onItemUse(final ItemUseContext context) {
        return tryOpenManual(context.getWorld(), context.getPlayer(), ManualAPI.pathFor(context.getWorld(), context.getPos())) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        if (world.isRemote) {
            if (player.isSneaking()) {
                ManualAPI.reset();
            }
            ManualAPI.openFor(player);
        }
        return super.onItemRightClick(world, player, hand);
    }

    // --------------------------------------------------------------------- //
    // ItemBook

    @Override
    public boolean isEnchantable(final ItemStack stack) {
        return false;
    }

    @Override
    public int getItemEnchantability() {
        return 0;
    }
}
