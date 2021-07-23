package li.cil.tis3d.common.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Base item for all modules.
 */
public class ModuleItem extends ModItem {
    public ModuleItem() {
    }

    public ModuleItem(final Properties properties) {
        super(properties);
    }

    // --------------------------------------------------------------------- //
    // Item

    @Nullable
    @Override
    public CompoundTag getShareTag(final ItemStack stack) {
        return null;
    }
}
