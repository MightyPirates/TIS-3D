package li.cil.tis3d.common.item;

import net.minecraft.item.Item;

/**
 * Base item for all modules.
 */
public class ItemModule extends Item {
    public ItemModule(Settings builder) {
        super(builder);
    }
    // --------------------------------------------------------------------- //
    // Item

    @Override
    public boolean requiresClientSync() {
        return false;
    }
}
