package li.cil.tis3d.common.item;

import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.util.FontRendererUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipOptions;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.TextComponent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The manual!
 */
public final class ManualBookItem extends BookItem {
    public ManualBookItem(Item.Settings builder) {
        super(builder);
    }

    public static boolean tryOpenManual(final World world, final PlayerEntity player, @Nullable final String path) {
        if (path == null) {
            return false;
        }

        if (world.isClient) {
            ManualAPI.openFor(player);
            ManualAPI.reset();
            ManualAPI.navigate(path);
        }

        return true;
    }

    // --------------------------------------------------------------------- //
    // Item

    @Environment(EnvType.CLIENT)
    @Override
    public void buildTooltip(final ItemStack stack, @Nullable final World world, final List<TextComponent> tooltip, final TooltipOptions flag) {
        super.buildTooltip(stack, world, tooltip, flag);
        final String info = I18n.translate(Constants.TOOLTIP_BOOK_MANUAL);
        FontRendererUtils.addStringToTooltip(info, tooltip);
    }

    @Override
    public ActionResult useOnBlock(final ItemUsageContext context) {
        return tryOpenManual(context.getWorld(), context.getPlayer(), ManualAPI.pathFor(context.getWorld(), context.getPos())) ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(final World world, final PlayerEntity player, final Hand hand) {
        if (world.isClient) {
            if (player.isSneaking()) {
                ManualAPI.reset();
            }
            ManualAPI.openFor(player);
        }
        return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
    }

    // --------------------------------------------------------------------- //
    // ItemBook

    @Override
    public boolean isTool(final ItemStack stack) {
        return false;
    }

    @Override
    public int getEnchantability() {
        return 0;
    }
}
