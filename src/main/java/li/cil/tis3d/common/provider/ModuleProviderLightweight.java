package li.cil.tis3d.common.provider;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.common.item.ItemModuleLightweight;
import li.cil.tis3d.common.module.ModuleLightweightHost;
import net.minecraft.item.ItemStack;

/**
 * Provider for the Lightweight module system.
 */
public class ModuleProviderLightweight implements ModuleProvider {
    @Override
    public boolean worksWith(ItemStack stack, Casing casing, Face face) {
        return stack.getItem() instanceof ItemModuleLightweight;
    }

    @Override
    public Module createModule(ItemStack stack, Casing casing, Face face) {
        String selected = ItemModuleLightweight.getSelectedStrFromStack(stack);
        if (selected == null)
            selected = "";
        return new ModuleLightweightHost(casing, face, selected);
    }
}
