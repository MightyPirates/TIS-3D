package li.cil.tis3d.api.detail;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.ModuleProvider;
import net.minecraft.item.ItemStack;
import javax.annotation.Nullable;

/**
 * Allows registering {@link ModuleProvider}s.
 * <p>
 * When trying to create a module instance for an {@link net.minecraft.item.ItemStack},
 * all registered providers will be queried, until one returns something not <tt>null</tt>.
 */
public interface ModuleAPI {
    /**
     * Register the specified provider.
     *
     * @param provider the provider to register.
     */
    void addProvider(final ModuleProvider provider);

    /**
     * Find the first provider supporting the specified item stack.
     *
     * @param stack  the item stack to find a provider for.
     * @param casing the casing the module would be installed in.
     * @param face   the face the module would be installed on.
     * @return the first provider supporting the item stack, or <tt>null</tt>.
     */
    @Nullable
    ModuleProvider getProviderFor(final ItemStack stack, final Casing casing, final Face face);
}
