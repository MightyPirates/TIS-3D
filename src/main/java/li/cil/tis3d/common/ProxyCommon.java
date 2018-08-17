package li.cil.tis3d.common;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.ModuleAPI;
import li.cil.tis3d.api.prefab.manual.ResourceContentProvider;
import li.cil.tis3d.client.manual.provider.GameRegistryPathProvider;
import li.cil.tis3d.common.api.*;
import li.cil.tis3d.common.capabilities.CapabilityInfraredReceiver;
import li.cil.tis3d.common.entity.EntityInfraredPacket;
import li.cil.tis3d.common.event.TickHandlerInfraredPacket;
import li.cil.tis3d.common.event.WorldUnloadHandler;
import li.cil.tis3d.common.gui.GuiHandlerCommon;
import li.cil.tis3d.common.init.Blocks;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.integration.Integration;
import li.cil.tis3d.common.integration.redstone.RedstoneIntegration;
import li.cil.tis3d.common.module.*;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.provider.SimpleModuleProvider;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.dimdev.rift.listener.EntityTypeAdder;
import org.dimdev.rift.listener.MinecraftStartListener;
import pl.asie.protocharset.rift.listeners.BootstrapListener;

/**
 * Takes care of common setup.
 */
public class ProxyCommon implements BootstrapListener, EntityTypeAdder {
    @Override
    public void afterBootstrap() {
        // Load our settings first to have all we need for remaining init.
        // TODO
        // Settings.load(event.getSuggestedConfigurationFile());

        // Initialize API.
        API.creativeTab = new CreativeTab();

        API.fontRendererAPI = new FontRendererAPIImpl();
        API.infraredAPI = new InfraredAPIImpl();
        API.manualAPI = ManualAPIImpl.INSTANCE;
        API.moduleAPI = new ModuleAPIImpl();
        API.serialAPI = SerialAPIImpl.INSTANCE;

        // Initialize capabilities.
        // TODO
        // CapabilityInfraredReceiver.register();

        // Register GUI handler for fancy GUIs in our almost GUI-less mod!
        // TODO
        //NetworkRegistry.INSTANCE.registerGuiHandler(TIS3D.instance, new GuiHandlerCommon());

        // Register providers for built-in modules.
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_AUDIO, ModuleAudio::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_BUNDLED_REDSTONE, ModuleBundledRedstone::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_DISPLAY, ModuleDisplay::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_EXECUTION, ModuleExecution::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_INFRARED, ModuleInfrared::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_KEYPAD, ModuleKeypad::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_QUEUE, ModuleQueue::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_RANDOM, ModuleRandom::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_RANDOM_ACCESS_MEMORY, ModuleRandomAccessMemory::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_READ_ONLY_MEMORY, ModuleReadOnlyMemory::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_REDSTONE, ModuleRedstone::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_SEQUENCER, ModuleSequencer::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_SERIAL_PORT, ModuleSerialPort::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_STACK, ModuleStack::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_TERMINAL, ModuleTerminal::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_TIMER, ModuleTimer::new));

        // Add default manual providers for server side stuff.
        ManualAPI.addProvider(new GameRegistryPathProvider());
        ManualAPI.addProvider(new ResourceContentProvider(API.MOD_ID, "doc/"));
        ManualAPI.addProvider(SerialAPIImpl.INSTANCE.getSerialProtocolContentProvider());

        // Mod integration.
        Integration.init();
    }

    @Override
    public void registerEntityTypes() {
        EntityInfraredPacket.TYPE = EntityType.register("tis3d:infrared_packet", EntityType.Builder.create(
                EntityInfraredPacket.class, EntityInfraredPacket::new
        ));
    }
 /*   public void onPreInit(final FMLPreInitializationEvent event) {
        // Load our settings first to have all we need for remaining init.
        Settings.load(event.getSuggestedConfigurationFile());

        // Initialize API.
        API.creativeTab = new CreativeTab();

        API.fontRendererAPI = new FontRendererAPIImpl();
        API.infraredAPI = new InfraredAPIImpl();
        API.manualAPI = ManualAPIImpl.INSTANCE;
        API.moduleAPI = new ModuleAPIImpl();
        API.serialAPI = SerialAPIImpl.INSTANCE;

        // Initialize capabilities.
        CapabilityInfraredReceiver.register();

        // Register GUI handler for fancy GUIs in our almost GUI-less mod!
        NetworkRegistry.INSTANCE.registerGuiHandler(TIS3D.instance, new GuiHandlerCommon());

        // Mod integration.
        Integration.preInit(event);
    }

    public void onInit(final FMLInitializationEvent event) {
        // Register Ore Dictionary entries.
        OreDictionary.registerOre("book", Items.bookCode);
        OreDictionary.registerOre("book", Items.bookManual);

        for (final String module : Constants.MODULES) {
            registerModuleOre(module);
        }

        // Register entities.
        EntityRegistry.registerModEntity(new ResourceLocation(API.MOD_ID, Constants.NAME_ENTITY_INFRARED_PACKET), EntityInfraredPacket.class, Constants.NAME_ENTITY_INFRARED_PACKET, 1, TIS3D.instance, 16, 1, true);

        // Register network handler.
        Network.INSTANCE.init();

        // Register event handlers.
        MinecraftForge.EVENT_BUS.register(Network.INSTANCE);
        MinecraftForge.EVENT_BUS.register(RedstoneIntegration.INSTANCE);
        MinecraftForge.EVENT_BUS.register(TickHandlerInfraredPacket.INSTANCE);
        MinecraftForge.EVENT_BUS.register(WorldUnloadHandler.INSTANCE);

        // Register providers for built-in modules.
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_AUDIO, ModuleAudio::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_BUNDLED_REDSTONE, ModuleBundledRedstone::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_DISPLAY, ModuleDisplay::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_EXECUTION, ModuleExecution::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_INFRARED, ModuleInfrared::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_KEYPAD, ModuleKeypad::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_QUEUE, ModuleQueue::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_RANDOM, ModuleRandom::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_RANDOM_ACCESS_MEMORY, ModuleRandomAccessMemory::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_READ_ONLY_MEMORY, ModuleReadOnlyMemory::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_REDSTONE, ModuleRedstone::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_SEQUENCER, ModuleSequencer::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_SERIAL_PORT, ModuleSerialPort::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_STACK, ModuleStack::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_TERMINAL, ModuleTerminal::new));
        ModuleAPI.addProvider(new SimpleModuleProvider<>(Constants.NAME_ITEM_MODULE_TIMER, ModuleTimer::new));

        // Add default manual providers for server side stuff.
        ManualAPI.addProvider(new GameRegistryPathProvider());
        ManualAPI.addProvider(new ResourceContentProvider(API.MOD_ID, "doc/"));
        ManualAPI.addProvider(SerialAPIImpl.INSTANCE.getSerialProtocolContentProvider());

        // Mod integration.
        Integration.init(event);
    }

    public void onPostInit(final FMLPostInitializationEvent event) {
        // Mod integration.
        Integration.postInit(event);
    }

    // --------------------------------------------------------------------- //

    @SubscribeEvent
    public static void handleRegisterBlocksEvent(final RegistryEvent.Register<Block> event) {
        Blocks.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void handleRegisterItemsEvent(final RegistryEvent.Register<Item> event) {
        Items.register(event.getRegistry());
    }

    // --------------------------------------------------------------------- //

    private static void registerModuleOre(final String name) {
        if (Settings.disabledModules.contains(name)) {
            return;
        }

        OreDictionary.registerOre(API.MOD_ID + ":module", Item.REGISTRY.getObject(new ResourceLocation(API.MOD_ID, name)));
    } */
}
