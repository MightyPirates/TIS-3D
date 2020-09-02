package li.cil.tis3d.common.item;

import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.LocalAPI;
import li.cil.tis3d.util.FontRendererUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
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
    public ManualBookItem(final Item.Settings settings) {
        super(settings);
    }

    public static boolean tryOpenManual(final World world, final PlayerEntity player, @Nullable final String path) {
        if (path == null) {
            return false;
        }

        if (world.isClient) {
            LocalAPI.common.manualAPI.openFor(player);
            LocalAPI.common.manualAPI.reset();
            LocalAPI.common.manualAPI.navigate(path);
        }

        return true;
    }

    // --------------------------------------------------------------------- //
    // Item

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(final ItemStack stack, @Nullable final World world, final List<Text> tooltip, final TooltipContext options) {
        super.appendTooltip(stack, world, tooltip, options);
        final String info = I18n.translate(Constants.TOOLTIP_BOOK_MANUAL);
        FontRendererUtils.addStringToTooltip(info, tooltip);
    }

    @Override
    public ActionResult useOnBlock(final ItemUsageContext context) {
        final PlayerEntity player = context.getPlayer();
        if (player == null) {
            return super.useOnBlock(context);
        }
        return tryOpenManual(context.getWorld(), player, LocalAPI.common.manualAPI.pathFor(context.getWorld(), context.getBlockPos())) ? ActionResult.SUCCESS : super.useOnBlock(context);
    }

    @Override
    public TypedActionResult<ItemStack> use(final World world, final PlayerEntity player, final Hand hand) {
        if (world.isClient) {
            if (player.isSneaking()) {
                LocalAPI.common.manualAPI.reset();
            }
            LocalAPI.common.manualAPI.openFor(player);
        }
        return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
    }

    // --------------------------------------------------------------------- //
    // ItemBook

    @Override
    public boolean isEnchantable(final ItemStack stack) {
        return false;
    }

    @Override
    public int getEnchantability() {
        return 0;
    }
}
