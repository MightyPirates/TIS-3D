package li.cil.tis3d.common.provider;

import li.cil.tis3d.Constants;
import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.system.module.ModuleRedstone;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * The provider for the redstone module.
 */
public final class ModuleProviderRedstone implements ModuleProvider {
    private final Item ItemModuleRedstone = GameRegistry.findItem(Constants.MOD_ID, Constants.ItemModuleRedstoneName);

    @Override
    public boolean worksWith(final ItemStack stack, final Casing casing, final Face face) {
        return stack.getItem() == ItemModuleRedstone;
    }

    @Override
    public Module createModule(final ItemStack stack, final Casing casing, final Face face) {
        return new ModuleRedstone(casing, face);
    }
}
