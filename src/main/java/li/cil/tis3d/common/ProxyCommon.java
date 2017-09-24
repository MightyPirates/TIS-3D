package li.cil.tis3d.common;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.ModuleAPI;
import li.cil.tis3d.api.prefab.manual.ResourceContentProvider;
import li.cil.tis3d.client.manual.provider.GameRegistryPathProvider;
import li.cil.tis3d.common.api.CreativeTab;
import li.cil.tis3d.common.api.FontRendererAPIImpl;
import li.cil.tis3d.common.api.InfraredAPIImpl;
import li.cil.tis3d.common.api.ManualAPIImpl;
import li.cil.tis3d.common.api.ModuleAPIImpl;
import li.cil.tis3d.common.api.SerialAPIImpl;
import li.cil.tis3d.common.capabilities.CapabilityInfraredReceiver;
import li.cil.tis3d.common.entity.EntityInfraredPacket;
import li.cil.tis3d.common.event.TickHandlerInfraredPacket;
import li.cil.tis3d.common.event.WorldUnloadHandler;
import li.cil.tis3d.common.gui.GuiHandlerCommon;
import li.cil.tis3d.common.init.Blocks;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.integration.Integration;
import li.cil.tis3d.common.integration.redstone.RedstoneIntegration;
import li.cil.tis3d.common.item.ItemModule;
import li.cil.tis3d.common.item.ItemModuleReadOnlyMemory;
import li.cil.tis3d.common.module.ModuleAudio;
import li.cil.tis3d.common.module.ModuleBundledRedstone;
import li.cil.tis3d.common.module.ModuleDisplay;
import li.cil.tis3d.common.module.ModuleExecution;
import li.cil.tis3d.common.module.ModuleInfrared;
import li.cil.tis3d.common.module.ModuleKeypad;
import li.cil.tis3d.common.module.ModuleQueue;
import li.cil.tis3d.common.module.ModuleRandom;
import li.cil.tis3d.common.module.ModuleRandomAccessMemory;
import li.cil.tis3d.common.module.ModuleReadOnlyMemory;
import li.cil.tis3d.common.module.ModuleRedstone;
import li.cil.tis3d.common.module.ModuleSerialPort;
import li.cil.tis3d.common.module.ModuleStack;
import li.cil.tis3d.common.module.ModuleTerminal;
import li.cil.tis3d.common.module.ModuleTimer;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.provider.SimpleModuleProvider;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Takes care of common setup.
 */
public class ProxyCommon {
    public void onPreInit(final FMLPreInitializationEvent event) {
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

        // Register blocks and items.
        Blocks.registerBlocks(this);
        Items.register(this);

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

        // Hardcoded recipes!
        Blocks.addRecipes();
        Items.addRecipes();

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

    public Block registerBlock(final String name, final Supplier<Block> constructor, final Class<? extends TileEntity> tileEntity) {
        final Block block = constructor.get().
                setHardness(5).
                setResistance(10).
                setUnlocalizedName(API.MOD_ID + "." + name).
                setCreativeTab(API.creativeTab).
                setRegistryName(name);
        GameRegistry.register(block);
        GameRegistry.registerTileEntityWithAlternatives(tileEntity, API.MOD_ID + ": " + name, name);

        final Item itemBlock = new ItemBlock(block).
                setRegistryName(name);
        GameRegistry.register(itemBlock);

        return block;
    }

    public Item registerItem(final String name, final Supplier<Item> constructor) {
        final Item item = constructor.get().
                setUnlocalizedName(API.MOD_ID + "." + name).
                setCreativeTab(API.creativeTab).
                setRegistryName(name);
        GameRegistry.register(item);
        return item;
    }

    @Nullable
    public Item registerModule(final String name) {
        if (Settings.disabledModules.contains(name)) {
            return null;
        }

        if (Objects.equals(name, Constants.NAME_ITEM_MODULE_READ_ONLY_MEMORY)) {
            return registerItem(name, ItemModuleReadOnlyMemory::new);
        } else {
            return registerItem(name, ItemModule::new);
        }
    }

    // --------------------------------------------------------------------- //

    private static void registerModuleOre(final String name) {
        if (Settings.disabledModules.contains(name)) {
            return;
        }

        OreDictionary.registerOre(API.MOD_ID + ":module", Item.REGISTRY.getObject(new ResourceLocation(API.MOD_ID, name)));
    }
}
