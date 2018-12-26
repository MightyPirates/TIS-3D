package li.cil.tis3d.common.init;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.ModuleAPI;
import li.cil.tis3d.api.prefab.manual.ResourceContentProvider;
import li.cil.tis3d.client.manual.provider.GameRegistryPathProvider;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.api.*;
import li.cil.tis3d.common.event.TickHandlerInfraredPacket;
import li.cil.tis3d.common.integration.Integration;
import li.cil.tis3d.common.module.*;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.module.provider.SimpleModuleProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.events.TickEvent;
import net.fabricmc.loader.FabricLoader;
import net.minecraft.item.ItemGroup;

@SuppressWarnings("unused")
public final class BootstrapCommon implements ModInitializer {
    @Override
    public void onInitialize() {
        // Load our settings first to have all we need for remaining init.
        Settings.load(FabricLoader.INSTANCE.getConfigDirectory());

        // Initialize API.
        API.creativeTab = ItemGroup.REDSTONE;

        API.fontRendererAPI = new FontRendererAPIImpl();
        API.infraredAPI = new InfraredAPIImpl();
        API.manualAPI = ManualAPIImpl.INSTANCE;
        API.moduleAPI = new ModuleAPIImpl();
        API.serialAPI = SerialAPIImpl.INSTANCE;

        // Register network handler.
        Network.INSTANCE.init();

        // Register event handlers.
        TickEvent.SERVER.register(server -> TickHandlerInfraredPacket.INSTANCE.serverTick());
        TickEvent.SERVER.register(server -> Network.INSTANCE.serverTick());

        // Register entities.
        Entities.registerEntities();

        // Register blocks.
        Blocks.registerBlocks();
        Blocks.registerBlockEntityTypes();

        // Register items.
        Items.registerItems();

        // Register providers for built-in modules.
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_AUDIO, AudioModule::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_BUNDLED_REDSTONE, BundledRedstoneModule::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_DISPLAY, DisplayModule::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_EXECUTION, ExecutionModule::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_INFRARED, InfraredModule::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_KEYPAD, KeypadModule::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_QUEUE, QueueModule::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_RANDOM, RandomModule::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_RANDOM_ACCESS_MEMORY, RandomAccessMemoryModule::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_READ_ONLY_MEMORY, ReadOnlyMemoryModule::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_REDSTONE, RedstoneModule::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_SEQUENCER, SequencerModule::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_SERIAL_PORT, SerialPortModule::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_STACK, StackModule::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_TERMINAL, TerminalModule::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_TIMER, TimerModule::new));

        // Add default manual providers for server side stuff.
        ManualAPI.addProvider(new GameRegistryPathProvider());
        ManualAPI.addProvider(new ResourceContentProvider(API.MOD_ID, "doc/"));
        ManualAPI.addProvider(SerialAPIImpl.INSTANCE.getSerialProtocolContentProvider());

        // Mod integration.
        Integration.init();
    }
}
