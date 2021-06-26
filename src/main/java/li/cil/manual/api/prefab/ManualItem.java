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
import net.minecraftforge.fml.RegistryObject;

public class ManualItem extends Item {
    private final RegistryObject<Manual> manualReference;

    // --------------------------------------------------------------------- //

    public ManualItem(final Properties properties, final RegistryObject<Manual> manualReference) {
        super(properties);
        this.manualReference = manualReference;
    }

    // --------------------------------------------------------------------- //
    // Item

    @Override
    public ActionResultType useOn(final ItemUseContext context) {
        final World world = context.getLevel();
        if (world.isClientSide()) {
            final Manual manual = manualReference.get();
            manual.reset();
            manual.pathFor(world, context.getClickedPos(), context.getClickedFace()).ifPresent(manual::push);
            manual.open();
        }
        return ActionResultType.sidedSuccess(world.isClientSide());
    }

    @Override
    public ActionResult<ItemStack> use(final World world, final PlayerEntity player, final Hand hand) {
        if (world.isClientSide()) {
            final Manual manual = manualReference.get();
            if (player.isShiftKeyDown()) {
                manual.reset();
            }
            manual.open();
        }
        return ActionResult.sidedSuccess(player.getItemInHand(hand), world.isClientSide());
    }
}
