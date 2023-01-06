package li.cil.tis3d.common.provider;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.RedstoneInputProvider;
import li.cil.tis3d.common.provider.redstone.MinecraftRedstoneInputProvider;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class RedstoneInputProviders {
    private static final DeferredRegister<RedstoneInputProvider> REDSTONE_INPUT_PROVIDERS = RegistryUtils.get(RedstoneInputProvider.REGISTRY);

    // --------------------------------------------------------------------- //

    public static final Registrar<RedstoneInputProvider> REDSTONE_INPUT_PROVIDER_REGISTRY = REDSTONE_INPUT_PROVIDERS.getRegistries()
        .<RedstoneInputProvider>builder(RedstoneInputProvider.REGISTRY.location())
        .build();

    // --------------------------------------------------------------------- //

    public static void initialize() {
        REDSTONE_INPUT_PROVIDERS.register("minecraft", MinecraftRedstoneInputProvider::new);

        REDSTONE_INPUT_PROVIDERS.register();
    }

    public static int getRedstoneInput(final Module module) {
        int maxSignal = 0;
        final Level level = module.getCasing().getCasingLevel();
        final BlockPos position = module.getCasing().getPosition();
        for (final RedstoneInputProvider provider : REDSTONE_INPUT_PROVIDER_REGISTRY) {
            final int signal = provider.getInput(level, position, Face.toDirection(module.getFace()));
            if (signal > maxSignal) {
                maxSignal = signal;
            }
        }
        return maxSignal;
    }
}
