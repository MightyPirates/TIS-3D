package li.cil.tis3d.common.item;

import li.cil.manual.api.Manual;
import li.cil.tis3d.client.manual.Manuals;
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
            final Manual manual = Manuals.MANUAL.get();
            manual.reset();
            manual.pathFor(world, context.getClickedPos(), context.getClickedFace()).ifPresent(manual::push);
            manual.open();
        }
        return ActionResultType.sidedSuccess(world.isClientSide());
    }

    @Override
    public ActionResult<ItemStack> use(final World world, final PlayerEntity player, final Hand hand) {
        if (world.isClientSide()) {
            final Manual manual = Manuals.MANUAL.get();
            if (player.isShiftKeyDown()) {
                manual.reset();
            }
            manual.open();
        }
        return ActionResult.sidedSuccess(player.getItemInHand(hand), world.isClientSide());
    }
}
