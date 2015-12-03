package li.cil.tis3d.api;

import li.cil.tis3d.api.detail.Registry;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.common.CreativeTab;
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
     * Find the first provider supporting the specified item stack.
     *
     * @param stack  the item stack to find a provider for.
     * @param casing the casing the module would be installed in.
     * @param face   the face the module would be installed on.
     * @return the first provider supporting the item stack, or <tt>null</tt>.
     */
    public static ModuleProvider getProviderFor(final ItemStack stack, final Casing casing, final Face face) {
        if (instance != null)
            return instance.getProviderFor(stack, casing, face);
        return null;
    }

    // --------------------------------------------------------------------- //

    public static Registry instance;
    public static CreativeTab creativeTab;

    private API() {
    }
}
