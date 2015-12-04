package li.cil.tis3d.common.provider;

import li.cil.tis3d.Constants;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.system.module.ModuleStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * The provider for the stack module.
 */
public final class ModuleProviderStack implements ModuleProvider {
    private final Item ITEM_MODULE_STACK = GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_STACK);

    @Override
    public boolean worksWith(final ItemStack stack, final Casing casing, final Face face) {
        return stack.getItem() == ITEM_MODULE_STACK;
    }

    @Override
    public Module createModule(final ItemStack stack, final Casing casing, final Face face) {
        return new ModuleStack(casing, face);
    }
}
