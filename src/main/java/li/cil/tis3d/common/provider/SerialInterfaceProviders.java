package li.cil.tis3d.common.provider;

import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.common.provider.serial.SerialInterfaceProviderFurnace;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.Optional;
import java.util.function.Supplier;

public final class SerialInterfaceProviders {
    private static final DeferredRegister<SerialInterfaceProvider> MODULE_PROVIDERS = RegistryUtils.create(SerialInterfaceProvider.class);

    // --------------------------------------------------------------------- //

    public static final Supplier<IForgeRegistry<SerialInterfaceProvider>> MODULE_PROVIDER_REGISTRY = MODULE_PROVIDERS.makeRegistry("serial_interfaces", RegistryBuilder::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        MODULE_PROVIDERS.register("furnace", SerialInterfaceProviderFurnace::new);
    }

    public static Optional<SerialInterfaceProvider> getProviderFor(final World world, final BlockPos position, final Direction face) {
        for (final SerialInterfaceProvider provider : MODULE_PROVIDER_REGISTRY.get()) {
            if (provider.matches(world, position, face)) {
                return Optional.of(provider);
            }
        }
        return Optional.empty();
    }
}
