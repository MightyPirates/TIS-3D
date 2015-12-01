package li.cil.tis3d.api;

import li.cil.tis3d.api.detail.Registry;
import li.cil.tis3d.api.module.ModuleProvider;
import net.minecraft.item.ItemStack;

/**
 * Entry point for the TIS-3D API.
 */
public final class API {
    /**
     * Register the specified provider.
     *
     * @param provider the provider to register.
     */
    public static void addProvider(final ModuleProvider provider) {
        if (instance != null)
            instance.addProvider(provider);
    }

    /**
     * Find the first provider supporting the specified stack.
     *
     * @param stack  the stack to find a provider for.
     * @param casing the casing the module would be installed in.
     * @param face   the face the module would be installed on.
     * @return the first provider supporting the stack, or <tt>null</tt>.
     */
    public static ModuleProvider providerFor(final ItemStack stack, final Casing casing, final Face face) {
        if (instance != null)
            return instance.providerFor(stack, casing, face);
        return null;
    }

    // --------------------------------------------------------------------- //

    public static Registry instance;

    private API() {
    }
}
