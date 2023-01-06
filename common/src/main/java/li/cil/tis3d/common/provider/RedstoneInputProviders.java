package li.cil.tis3d.common.provider;

import com.google.common.base.Suppliers;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.Registries;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.RedstoneInputProvider;
import li.cil.tis3d.common.provider.redstone.MinecraftRedstoneInputProvider;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public final class RedstoneInputProviders {
    private static final DeferredRegister<RedstoneInputProvider> REDSTONE_INPUT_PROVIDERS = RegistryUtils.get(RedstoneInputProvider.REGISTRY);

    private static final Supplier<Registrar<RedstoneInputProvider>> REGISTRAR = Suppliers.memoize(() -> Registries.get(API.MOD_ID).get(RedstoneInputProvider.REGISTRY));

    // --------------------------------------------------------------------- //

    public static void initialize() {
        RegistryUtils.builder(RedstoneInputProvider.REGISTRY).build();

        REDSTONE_INPUT_PROVIDERS.register("minecraft", MinecraftRedstoneInputProvider::new);

        REDSTONE_INPUT_PROVIDERS.register();
    }

    public static int getRedstoneInput(final Module module) {
        int maxSignal = 0;
        final Level level = module.getCasing().getCasingLevel();
        final BlockPos position = module.getCasing().getPosition();
        for (final RedstoneInputProvider provider : REGISTRAR.get()) {
            final int signal = provider.getInput(level, position, Face.toDirection(module.getFace()));
            if (signal > maxSignal) {
                maxSignal = signal;
            }
        }
        return maxSignal;
    }
}
