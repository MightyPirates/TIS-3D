package li.cil.tis3d.api;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.ModuleProvider;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * API entry point for registering {@link ModuleProvider}s and other
 * module related tasks.
 * <p>
 * When trying to create a module instance for an {@link net.minecraft.item.ItemStack},
 * all registered providers will be queried, until one returns something not <tt>null</tt>.
 * <p>
 * This is made available in the init phase, so you'll either have to (soft)
 * depend on TIS-3D or you must not make calls to this before the init phase.
 */
public final class ModuleAPI {
    /**
     * Register the specified provider.
     *
     * @param provider the provider to register.
     */
    public static void addProvider(final ModuleProvider provider) {
        if (API.moduleAPI != null) {
            API.moduleAPI.addProvider(provider);
        }
    }

    /**
     * Find the first provider supporting the specified item stack.
     *
     * @param stack  the item stack to find a provider for.
     * @param casing the casing the module would be installed in.
     * @param face   the face the module would be installed on.
     * @return the first provider supporting the item stack, or <tt>null</tt>.
     */
    @Nullable
    public static ModuleProvider getProviderFor(final ItemStack stack, final Casing casing, final Face face) {
        if (API.moduleAPI != null) {
            return API.moduleAPI.getProviderFor(stack, casing, face);
        }
        return null;
    }

    // --------------------------------------------------------------------- //

    private ModuleAPI() {
    }
}
