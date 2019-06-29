package li.cil.tis3d.common.item;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.block.CasingBlock;
import li.cil.tis3d.util.FontRendererUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Base item for all keys.
 */
public final class KeyItem extends Item {
    public KeyItem(final Item.Settings settings) {
        super(settings.maxCount(1));
    }

    // --------------------------------------------------------------------- //
    // Item

    @Override
    public boolean shouldSyncTagToClient() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(final ItemStack stack, @Nullable final World world, final List<Text> tooltip, final TooltipContext options) {
        super.appendTooltip(stack, world, tooltip, options);
        final String info = I18n.translate(Constants.TOOLTIP_KEY);
        FontRendererUtils.addStringToTooltip(info, tooltip);
    }

    @Override
    public ActionResult useOnBlock(final ItemUsageContext context) {
        return CasingBlock.activate(context) ? ActionResult.SUCCESS : super.useOnBlock(context);
    }

    @Override
    public boolean isEnchantable(final ItemStack stack) {
        return false;
    }
}
