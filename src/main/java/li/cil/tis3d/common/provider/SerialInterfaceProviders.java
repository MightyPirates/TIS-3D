package li.cil.tis3d.common.provider;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.common.provider.serial.SerialInterfaceProviderFurnace;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public final class SerialInterfaceProviders {
    private static final DeferredRegister<SerialInterfaceProvider> MODULE_PROVIDERS = DeferredRegister.create(SerialInterfaceProvider.class, API.MOD_ID);

    // --------------------------------------------------------------------- //

    public static final Supplier<IForgeRegistry<SerialInterfaceProvider>> MODULE_PROVIDER_REGISTRY = MODULE_PROVIDERS.makeRegistry("serial_interfaces", RegistryBuilder::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        MODULE_PROVIDERS.register("furnace", SerialInterfaceProviderFurnace::new);

        MODULE_PROVIDERS.register(FMLJavaModLoadingContext.get().getModEventBus());
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
