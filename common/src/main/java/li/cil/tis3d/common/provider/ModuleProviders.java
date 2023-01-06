package li.cil.tis3d.common.provider;

import com.google.common.base.Suppliers;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.Registries;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.item.ModuleItem;
import li.cil.tis3d.common.module.*;
import li.cil.tis3d.common.provider.module.SimpleModuleProvider;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class ModuleProviders {
    private static final DeferredRegister<ModuleProvider> MODULE_PROVIDERS = RegistryUtils.get(ModuleProvider.REGISTRY);

    // --------------------------------------------------------------------- //

    private static final Supplier<Registrar<ModuleProvider>> REGISTRAR = Suppliers.memoize(() -> Registries.get(API.MOD_ID).get(ModuleProvider.REGISTRY));

    // --------------------------------------------------------------------- //

    public static void initialize() {
        RegistryUtils.builder(ModuleProvider.REGISTRY).build();

        register(Items.AUDIO_MODULE, AudioModule::new);
        register(Items.DISPLAY_MODULE, DisplayModule::new);
        register(Items.EXECUTION_MODULE, ExecutionModule::new);
        register(Items.FACADE_MODULE, FacadeModule::new);
        register(Items.INFRARED_MODULE, InfraredModule::new);
        register(Items.KEYPAD_MODULE, KeypadModule::new);
        register(Items.QUEUE_MODULE, QueueModule::new);
        register(Items.RANDOM_MODULE, RandomModule::new);
        register(Items.RANDOM_ACCESS_MEMORY_MODULE, RandomAccessMemoryModule::new);
        register(Items.READ_ONLY_MEMORY_MODULE, ReadOnlyMemoryModule::new);
        register(Items.REDSTONE_MODULE, RedstoneModule::new);
        register(Items.SEQUENCER_MODULE, SequencerModule::new);
        register(Items.SERIAL_PORT_MODULE, SerialPortModule::new);
        register(Items.STACK_MODULE, StackModule::new);
        register(Items.TERMINAL_MODULE, TerminalModule::new);
        register(Items.TIMER_MODULE, TimerModule::new);

        MODULE_PROVIDERS.register();
    }

    public static Optional<ModuleProvider> getProviderFor(final ItemStack stack, final Casing casing, final Face face) {
        for (final ModuleProvider provider : REGISTRAR.get()) {
            if (provider.matches(stack, casing, face)) {
                return Optional.of(provider);
            }
        }
        return Optional.empty();
    }

    // --------------------------------------------------------------------- //

    private static <T extends Module> void register(final RegistrySupplier<? extends ModuleItem> item, final BiFunction<Casing, Face, T> factory) {
        MODULE_PROVIDERS.register(item.getId().getPath(), () -> new SimpleModuleProvider<>(item, factory));
    }
}
