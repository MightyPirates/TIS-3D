package li.cil.tis3d.api.detail;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.module.ModuleProvider;
import net.minecraft.item.ItemStack;

/**
 * Allows registering {@link ModuleProvider}s.
 * <p>
 * When trying to create a module instance for an {@link net.minecraft.item.ItemStack},
 * all registered providers will be queried, until one returns something not <tt>null</tt>.
 */
public interface Registry {
    /**
     * Register the specified provider.
     *
     * @param provider the provider to register.
     */
    void addProvider(ModuleProvider provider);

    /**
     * Find the first provider supporting the specified item stack.
     *
     * @param stack  the item stack to find a provider for.
     * @param casing the casing the module would be installed in.
     * @param face   the face the module would be installed on.
     * @return the first provider supporting the item stack, or <tt>null</tt>.
     */
    ModuleProvider getProviderFor(ItemStack stack, Casing casing, Face face);
}
