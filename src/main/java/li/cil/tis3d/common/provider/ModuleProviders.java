package li.cil.tis3d.common.provider;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.common.item.ModuleItem;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.module.*;
import li.cil.tis3d.common.provider.module.SimpleModuleProvider;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class ModuleProviders {
    private static final DeferredRegister<ModuleProvider> MODULE_PROVIDERS = RegistryUtils.create(ModuleProvider.class);

    // --------------------------------------------------------------------- //

    public static final Supplier<IForgeRegistry<ModuleProvider>> MODULE_PROVIDER_REGISTRY = MODULE_PROVIDERS.makeRegistry("modules", RegistryBuilder::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
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
    }

    public static Optional<ModuleProvider> getProviderFor(final ItemStack stack, final Casing casing, final Face face) {
        for (final ModuleProvider provider : MODULE_PROVIDER_REGISTRY.get()) {
            if (provider.matches(stack, casing, face)) {
                return Optional.of(provider);
            }
        }
        return Optional.empty();
    }

    // --------------------------------------------------------------------- //

    private static <T extends Module> void register(final RegistryObject<? extends ModuleItem> item, final BiFunction<Casing, Face, T> factory) {
        MODULE_PROVIDERS.register(item.getId().getPath(), () -> new SimpleModuleProvider<>(item, factory));
    }
}
