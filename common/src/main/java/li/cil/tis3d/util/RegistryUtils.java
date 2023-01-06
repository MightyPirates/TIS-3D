package li.cil.tis3d.util;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrarBuilder;
import dev.architectury.registry.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public abstract class RegistryUtils {
    private static String modId;

    @SafeVarargs
    public static <T> RegistrarBuilder<T> builder(ResourceKey<Registry<T>> registryKey, T... typeGetter) {
        if (modId == null) throw new IllegalStateException();
        return Registries.get(modId).builder(registryKey.location(), typeGetter);
    }

    public static <T> DeferredRegister<T> get(ResourceKey<Registry<T>> registryKey) {
        if (modId == null) throw new IllegalStateException();
        return DeferredRegister.create(modId, registryKey);
    }

    public static void initialize(final String modId) {
        if (modId.equals(RegistryUtils.modId)) return;
        RegistryUtils.modId = modId;
    }

    // --------------------------------------------------------------------- //

    private RegistryUtils() {
    }
}
