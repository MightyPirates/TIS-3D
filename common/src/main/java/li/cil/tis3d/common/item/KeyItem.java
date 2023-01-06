package li.cil.tis3d.common.item;

import li.cil.tis3d.common.block.CasingBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Base item for all keys.
 */
public final class KeyItem extends ModItem {
    public KeyItem() {
        super(createProperties().stacksTo(1));
    }

    // --------------------------------------------------------------------- //
    // Item

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        return CasingBlock.useIfCasing(context).orElseGet(() -> super.useOn(context));
    }
}
