package li.cil.tis3d.common.provider;

import com.google.common.base.Suppliers;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.Registries;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.common.provider.serial.SerialInterfaceProviderFurnace;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.Supplier;

public final class SerialInterfaceProviders {
    private static final DeferredRegister<SerialInterfaceProvider> MODULE_PROVIDERS = RegistryUtils.get(SerialInterfaceProvider.REGISTRY);

    // --------------------------------------------------------------------- //

    public static final Supplier<Registrar<SerialInterfaceProvider>> REGISTRAR = Suppliers.memoize(() -> Registries.get(API.MOD_ID).get(SerialInterfaceProvider.REGISTRY));

    // --------------------------------------------------------------------- //

    public static void initialize() {
        RegistryUtils.builder(SerialInterfaceProvider.REGISTRY).build();

        MODULE_PROVIDERS.register("furnace", SerialInterfaceProviderFurnace::new);

        MODULE_PROVIDERS.register();
    }

    public static Optional<SerialInterfaceProvider> getProviderFor(final Level level, final BlockPos position, final Direction face) {
        for (final SerialInterfaceProvider provider : REGISTRAR.get()) {
            if (provider.matches(level, position, face)) {
                return Optional.of(provider);
            }
        }
        return Optional.empty();
    }
}
