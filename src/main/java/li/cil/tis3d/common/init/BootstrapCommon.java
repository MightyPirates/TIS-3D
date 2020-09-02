package li.cil.tis3d.common.init;

import li.cil.tis3d.api.CommonAPI;
import li.cil.tis3d.api.ExtInitializer;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.ModuleAPI;
import li.cil.tis3d.client.manual.provider.GameRegistryPathProvider;
import li.cil.tis3d.common.API;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.api.*;
import li.cil.tis3d.common.event.TickHandlerInfraredPacket;
import li.cil.tis3d.common.integration.Integration;
import li.cil.tis3d.common.module.*;
import li.cil.tis3d.common.module.provider.SimpleModuleProvider;
import li.cil.tis3d.common.network.Network;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.util.List;

@SuppressWarnings("unused")
public final class BootstrapCommon implements ModInitializer {
    @Override
    public void onInitialize() {
        // Load our settings first to have all we need for remaining init.
        Settings.load(new File(FabricLoader.getInstance().getConfigDirectory(), API.MOD_ID + ".cfg"));

        // Initialize API.
        //noinspection Convert2MethodRef
        API.itemGroup = FabricItemGroupBuilder.create(Constants.NAME_ITEM_GROUP).
            // Gotta be a lambda or items get initialized before item group is set.
                icon(() -> Items.CONTROLLER.getStackForRender()).
                build();

        final ModuleAPI moduleAPI = new ModuleAPIImpl();

        API.infrared = new InfraredAPIImpl();
        API.manual = ManualAPIImpl.INSTANCE;
        API.module = moduleAPI;
        API.serial = SerialAPIImpl.INSTANCE;

        li.cil.tis3d.api.API.itemGroup = API.itemGroup;
        li.cil.tis3d.api.API.infraredAPI = API.infrared;
        li.cil.tis3d.api.API.manualAPI = API.manual;
        li.cil.tis3d.api.API.moduleAPI = API.module;
        li.cil.tis3d.api.API.serialAPI = API.serial;

        final CommonAPI commonAPI = new CommonAPI();
        commonAPI.itemGroup = API.itemGroup;
        commonAPI.infrared = API.infrared;
        commonAPI.manual = API.manual;
        commonAPI.module = API.module;
        commonAPI.serial = API.serial;

        // Register network handler.
        Network.INSTANCE.initServer();

        // Register event handlers.
        ServerTickCallback.EVENT.register(server -> TickHandlerInfraredPacket.INSTANCE.serverTick());
        ServerTickCallback.EVENT.register(server -> Network.INSTANCE.serverTick());

        // Register entities.
        Entities.registerEntities();

        // Register blocks.
        Blocks.registerBlocks();
        Blocks.registerBlockEntityTypes();

        // Register items.
        Items.registerItems();

        // Register providers for built-in modules.
        moduleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_AUDIO, AudioModule::new));
        moduleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_BUNDLED_REDSTONE, BundledRedstoneModule::new));
        moduleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_DISPLAY, DisplayModule::new));
        moduleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_EXECUTION, ExecutionModule::new));
        moduleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_INFRARED, InfraredModule::new));
        moduleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_KEYPAD, KeypadModule::new));
        moduleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_QUEUE, QueueModule::new));
        moduleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_RANDOM, RandomModule::new));
        moduleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_RANDOM_ACCESS_MEMORY, RandomAccessMemoryModule::new));
        moduleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_READ_ONLY_MEMORY, ReadOnlyMemoryModule::new));
        moduleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_REDSTONE, RedstoneModule::new));
        moduleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_SEQUENCER, SequencerModule::new));
        moduleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_SERIAL_PORT, SerialPortModule::new));
        moduleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_STACK, StackModule::new));
        moduleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_TERMINAL, TerminalModule::new));
        moduleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_TIMER, TimerModule::new));

        // Add default manual providers for server side stuff.
        ManualAPI.addProvider(new GameRegistryPathProvider());

        // Mod integration.
        Integration.init();

        // Initialize extensions.
        final String fullEntrypoint =  extensionEntrypoint("main");
        final List<ExtInitializer> extensions = FabricLoader.getInstance().getEntrypoints(fullEntrypoint, ExtInitializer.class);
        for (ExtInitializer initializer : extensions) {
            initializer.onInitialize(commonAPI);
        }
    }

    public static String extensionEntrypoint(final String entrypoint) {
        return API.MOD_ID + ":" + entrypoint;
    }
}
