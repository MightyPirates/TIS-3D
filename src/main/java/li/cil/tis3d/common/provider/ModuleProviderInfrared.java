package li.cil.tis3d.common.provider;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.module.ModuleInfrared;
import net.minecraft.item.ItemStack;

/**
 * The provider for the infrared module.
 */
public final class ModuleProviderInfrared implements ModuleProvider {
    @Override
    public boolean worksWith(final ItemStack stack, final Casing casing, final Face face) {
        return stack.getItem() == Items.modules.get(Constants.NAME_ITEM_MODULE_INFRARED);
    }

    @Override
    public Module createModule(final ItemStack stack, final Casing casing, final Face face) {
        return new ModuleInfrared(casing, face);
    }
}
