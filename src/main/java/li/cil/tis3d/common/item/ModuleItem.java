package li.cil.tis3d.common.item;

import net.minecraft.item.Item;

/**
 * Base item for all modules.
 */
public class ModuleItem extends Item {
    public ModuleItem(Settings builder) {
        super(builder);
    }

    // --------------------------------------------------------------------- //
    // Item

    @Override
    public boolean requiresClientSync() {
        return false;
    }
}
