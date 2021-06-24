package li.cil.tis3d.common.item;

import li.cil.tis3d.api.ManualAPI;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * The manual!
 */
public final class ItemBookManual extends ModItem {
    // --------------------------------------------------------------------- //
    // Item

    @Override
    public ActionResultType useOn(final ItemUseContext context) {
        final World world = context.getLevel();
        if (world.isClientSide()) {
            final String path = ManualAPI.pathFor(world, context.getClickedPos(), context.getClickedFace());
            if (path != null) {
                ManualAPI.open();
                ManualAPI.reset();
                ManualAPI.navigate(path);
            }
        }
        return ActionResultType.sidedSuccess(world.isClientSide());
    }

    @Override
    public ActionResult<ItemStack> use(final World world, final PlayerEntity player, final Hand hand) {
        if (world.isClientSide()) {
            if (player.isShiftKeyDown()) {
                ManualAPI.reset();
            }
            ManualAPI.open();
        }
        return ActionResult.sidedSuccess(player.getItemInHand(hand), world.isClientSide());
    }
}
