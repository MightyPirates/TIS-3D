package li.cil.tis3d.common.item;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.common.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;



import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base item for all keys.
 */
public final class ItemKey extends Item {
    public ItemKey(Item.Builder builder) {
        super(builder.maxStackSize(1));
    }

    // --------------------------------------------------------------------- //
    // Item

    @Override
    public boolean getShareTag() {
        return false;
    }


    @Override
    public void addInformation(final ItemStack stack, @Nullable final World world, final List<ITextComponent> tooltip, final ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        final String info = I18n.format(Constants.TOOLTIP_KEY);
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        tooltip.addAll(fontRenderer.listFormattedStringToWidth(info, li.cil.tis3d.common.Constants.MAX_TOOLTIP_WIDTH).stream().map(TextComponentString::new).collect(Collectors.toList()));
    }

    // TODO
    /* @Override
    public boolean doesSneakBypassUse(final ItemStack stack, final IBlockReader world, final BlockPos pos, final EntityPlayer player) {
        return world.getTileEntity(pos) instanceof Casing;
    } */

    @Override
    public boolean isEnchantable(final ItemStack stack) {
        return false;
    }
}
