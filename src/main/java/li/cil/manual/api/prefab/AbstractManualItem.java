package li.cil.manual.api.prefab;

import li.cil.manual.api.Manual;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractManualItem extends Item {
    protected AbstractManualItem(final Properties properties) {
        super(properties);
    }

    // --------------------------------------------------------------------- //
    // Item

    @Override
    public ActionResultType useOn(final ItemUseContext context) {
        final World world = context.getLevel();
        if (world.isClientSide()) {
            openManualFor(context, world);
        }
        return ActionResultType.sidedSuccess(world.isClientSide());
    }

    @Override
    public ActionResult<ItemStack> use(final World world, final PlayerEntity player, final Hand hand) {
        if (world.isClientSide()) {
            openManual(player);
        }
        return ActionResult.sidedSuccess(player.getItemInHand(hand), world.isClientSide());
    }

    // --------------------------------------------------------------------- //

    @OnlyIn(Dist.CLIENT)
    protected abstract Manual getManual();

    // --------------------------------------------------------------------- //

    @OnlyIn(Dist.CLIENT)
    private void openManualFor(final ItemUseContext context, final World world) {
        final Manual manual = getManual();
        manual.reset();
        manual.pathFor(world, context.getClickedPos(), context.getClickedFace()).ifPresent(manual::push);
        manual.open();
    }

    @OnlyIn(Dist.CLIENT)
    private void openManual(final PlayerEntity player) {
        final Manual manual = getManual();
        if (player.isShiftKeyDown()) {
            manual.reset();
        }
        manual.open();
    }
}
