package li.cil.tis3d.common.provider;

import li.cil.tis3d.Constants;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.system.module.ModuleRandom;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * The provider for the random value module.
 */
public final class ModuleProviderRandom implements ModuleProvider {
    private final Item ITEM_MODULE_RANDOM = GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_RANDOM);

    @Override
    public boolean worksWith(final ItemStack stack, final Casing casing, final Face face) {
        return stack.getItem() == ITEM_MODULE_RANDOM;
    }

    @Override
    public Module createModule(final ItemStack stack, final Casing casing, final Face face) {
        return new ModuleRandom(casing, face);
    }
}
