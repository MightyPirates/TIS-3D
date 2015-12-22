package li.cil.tis3d.common.provider;

import cpw.mods.fml.common.registry.GameRegistry;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.module.ModuleRandomAccessMemory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * The provider for the RAM module.
 */
public final class ModuleProviderRandomAccessMemory implements ModuleProvider {
    private final Item item = GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_RANDOM_ACCESS_MEMORY);

    @Override
    public boolean worksWith(final ItemStack stack, final Casing casing, final Face face) {
        return stack.getItem() == item;
    }

    @Override
    public Module createModule(final ItemStack stack, final Casing casing, final Face face) {
        return new ModuleRandomAccessMemory(casing, face);
    }
}
