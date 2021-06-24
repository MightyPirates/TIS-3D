package li.cil.tis3d.common.provider;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.common.item.ItemModule;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.module.*;
import li.cil.tis3d.common.provider.module.SimpleModuleProvider;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class ModuleProviders {
    private static final DeferredRegister<ModuleProvider> MODULE_PROVIDERS = DeferredRegister.create(ModuleProvider.class, API.MOD_ID);

    // --------------------------------------------------------------------- //

    public static final Supplier<IForgeRegistry<ModuleProvider>> MODULE_PROVIDER_REGISTRY = MODULE_PROVIDERS.makeRegistry("modules", RegistryBuilder::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        register(Items.AUDIO_MODULE, ModuleAudio::new);
        register(Items.DISPLAY_MODULE, ModuleDisplay::new);
        register(Items.EXECUTION_MODULE, ModuleExecution::new);
        register(Items.FACADE_MODULE, ModuleFacade::new);
        register(Items.INFRARED_MODULE, ModuleInfrared::new);
        register(Items.KEYPAD_MODULE, ModuleKeypad::new);
        register(Items.QUEUE_MODULE, ModuleQueue::new);
        register(Items.RANDOM_MODULE, ModuleRandom::new);
        register(Items.RANDOM_ACCESS_MEMORY_MODULE, ModuleRandomAccessMemory::new);
        register(Items.READ_ONLY_MEMORY_MODULE, ModuleReadOnlyMemory::new);
        register(Items.REDSTONE_MODULE, ModuleRedstone::new);
        register(Items.SEQUENCER_MODULE, ModuleSequencer::new);
        register(Items.SERIAL_PORT_MODULE, ModuleSerialPort::new);
        register(Items.STACK_MODULE, ModuleStack::new);
        register(Items.TERMINAL_MODULE, ModuleTerminal::new);
        register(Items.TIMER_MODULE, ModuleTimer::new);

        MODULE_PROVIDERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @Nullable
    public static ModuleProvider getProviderFor(final ItemStack stack, final Casing casing, final Face face) {
        for (final ModuleProvider provider : MODULE_PROVIDER_REGISTRY.get()) {
            if (provider.matches(stack, casing, face)) {
                return provider;
            }
        }
        return null;
    }

    // --------------------------------------------------------------------- //

    private static <T extends Module> void register(final RegistryObject<? extends ItemModule> item, final BiFunction<Casing, Face, T> factory) {
        MODULE_PROVIDERS.register(item.getId().getPath(), () -> new SimpleModuleProvider<>(item, factory));
    }
}
