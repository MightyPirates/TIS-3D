package li.cil.tis3d.common.item;

import net.minecraft.item.Item;

/**
 * Base item for all modules.
 */
public class ModuleItem extends Item {
    public ModuleItem(final Settings settings) {
        super(settings);
    }

    // --------------------------------------------------------------------- //
    // Item

    @Override
    public boolean shouldSyncTagToClient() {
        return false;
    }
}
