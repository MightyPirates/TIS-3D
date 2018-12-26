package li.cil.tis3d.common.item;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.block.CasingBlock;
import li.cil.tis3d.util.FontRendererUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipOptions;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.TextComponent;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Base item for all keys.
 */
public final class KeyItem extends Item {
    public KeyItem(final Item.Settings settings) {
        super(settings.stackSize(1));
    }

    // --------------------------------------------------------------------- //
    // Item

    @Override
    public boolean requiresClientSync() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void buildTooltip(final ItemStack stack, @Nullable final World world, final List<TextComponent> tooltip, final TooltipOptions flag) {
        super.buildTooltip(stack, world, tooltip, flag);
        final String info = I18n.translate(Constants.TOOLTIP_KEY);
        FontRendererUtils.addStringToTooltip(info, tooltip);
    }

    @Override
    public ActionResult useOnBlock(final ItemUsageContext context) {
        return CasingBlock.activate(context) ? ActionResult.SUCCESS : super.useOnBlock(context);
    }

    @Override
    public boolean isTool(final ItemStack stack) {
        return false;
    }
}
