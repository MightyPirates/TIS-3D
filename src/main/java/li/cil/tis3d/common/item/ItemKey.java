package li.cil.tis3d.common.item;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.util.FontRendererUtils;
import net.minecraft.client.item.TooltipOptions;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TextComponent;
import net.minecraft.world.World;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base item for all keys.
 */
public final class ItemKey extends Item {
    public ItemKey(Item.Settings builder) {
        super(builder.stackSize(1));
    }

    // --------------------------------------------------------------------- //
    // Item

    @Override
    public boolean requiresClientSync() {
        return false;
    }


    @Override
    public void buildTooltip(final ItemStack stack, @Nullable final World world, final List<TextComponent> tooltip, final TooltipOptions flag) {
        super.buildTooltip(stack, world, tooltip, flag);
        final String info = I18n.translate(Constants.TOOLTIP_KEY);
        FontRendererUtils.addStringToTooltip(info, tooltip);
    }

    // TODO
    /* @Override
    public boolean doesSneakBypassUse(final ItemStack stack, final IBlockReader world, final BlockPos pos, final EntityPlayer player) {
        return world.getTileEntity(pos) instanceof Casing;
    } */

    @Override
    public boolean isTool(final ItemStack stack) {
        return false;
    }
}
