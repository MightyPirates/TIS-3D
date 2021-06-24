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
    public ActionResultType onItemUse(final ItemUseContext context) {
        final World world = context.getWorld();
        if (world.isRemote()) {
            final String path = ManualAPI.pathFor(world, context.getPos(), context.getFace());
            if (path != null) {
                ManualAPI.open();
                ManualAPI.reset();
                ManualAPI.navigate(path);
            }
        }
        return ActionResultType.func_233537_a_(world.isRemote());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final PlayerEntity player, final Hand hand) {
        if (world.isRemote()) {
            if (player.isSneaking()) {
                ManualAPI.reset();
            }
            ManualAPI.open();
        }
        return ActionResult.func_233538_a_(player.getHeldItem(hand), world.isRemote());
    }
}
