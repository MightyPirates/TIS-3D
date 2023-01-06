package li.cil.tis3d.common.provider;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.common.provider.serial.SerialInterfaceProviderFurnace;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.Optional;

public final class SerialInterfaceProviders {
    private static final DeferredRegister<SerialInterfaceProvider> MODULE_PROVIDERS = RegistryUtils.get(SerialInterfaceProvider.REGISTRY);

    // --------------------------------------------------------------------- //

    public static final Registrar<SerialInterfaceProvider> MODULE_PROVIDER_REGISTRY = MODULE_PROVIDERS.getRegistries()
        .<SerialInterfaceProvider>builder(SerialInterfaceProvider.REGISTRY.location())
        .build();

    // --------------------------------------------------------------------- //

    public static void initialize() {
        MODULE_PROVIDERS.register("furnace", SerialInterfaceProviderFurnace::new);

        MODULE_PROVIDERS.register();
    }

    public static Optional<SerialInterfaceProvider> getProviderFor(final Level level, final BlockPos position, final Direction face) {
        for (final SerialInterfaceProvider provider : MODULE_PROVIDER_REGISTRY) {
            if (provider.matches(level, position, face)) {
                return Optional.of(provider);
            }
        }
        return Optional.empty();
    }
}
