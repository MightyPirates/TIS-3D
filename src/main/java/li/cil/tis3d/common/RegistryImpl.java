package li.cil.tis3d.common;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.detail.Registry;
import li.cil.tis3d.api.module.ModuleProvider;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry for module providers.
 * <p>
 * Allows registering providers as well as querying them.
 */
public final class RegistryImpl implements Registry {
    private final List<ModuleProvider> providers = new ArrayList<>();

    @Override
    public void addProvider(final ModuleProvider provider) {
        if (!providers.contains(provider)) {
            providers.add(provider);
        }
    }

    @Override
    public ModuleProvider getProviderFor(final ItemStack stack, final Casing casing, final Face face) {
        if (stack != null) {
            for (final ModuleProvider provider : providers) {
                if (provider.worksWith(stack, casing, face)) {
                    return provider;
                }
            }
        }
        return null;
    }
}
