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
import li.cil.tis3d.common.entity.EntityInfraredPacket;
import li.cil.tis3d.common.event.TickHandlerInfraredPacket;
import li.cil.tis3d.common.init.Blocks;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.integration.Integration;
import li.cil.tis3d.common.integration.redstone.RedstoneIntegration;
import li.cil.tis3d.common.item.ItemModule;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.provider.ModuleProviderAudio;
import li.cil.tis3d.common.provider.ModuleProviderBundledRedstone;
import li.cil.tis3d.common.provider.ModuleProviderDisplay;
import li.cil.tis3d.common.provider.ModuleProviderExecution;
import li.cil.tis3d.common.provider.ModuleProviderInfrared;
import li.cil.tis3d.common.provider.ModuleProviderKeypad;
import li.cil.tis3d.common.provider.ModuleProviderRandom;
import li.cil.tis3d.common.provider.ModuleProviderRandomAccessMemory;
import li.cil.tis3d.common.provider.ModuleProviderRedstone;
import li.cil.tis3d.common.provider.ModuleProviderSerialPort;
import li.cil.tis3d.common.provider.ModuleProviderStack;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

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

        // Register blocks and items.
        Blocks.registerBlocks(this);
        Items.register(this);

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
        EntityRegistry.registerModEntity(EntityInfraredPacket.class, Constants.NAME_ENTITY_INFRARED_PACKET, 1, TIS3D.instance, 16, 1, true);

        // Register network handler.
        Network.INSTANCE.init();

        // Register event handlers.
        MinecraftForge.EVENT_BUS.register(Network.INSTANCE);
        MinecraftForge.EVENT_BUS.register(RedstoneIntegration.INSTANCE);
        MinecraftForge.EVENT_BUS.register(TickHandlerInfraredPacket.INSTANCE);

        // Register providers for built-in modules.
        ModuleAPI.addProvider(new ModuleProviderAudio());
        ModuleAPI.addProvider(new ModuleProviderBundledRedstone());
        ModuleAPI.addProvider(new ModuleProviderDisplay());
        ModuleAPI.addProvider(new ModuleProviderExecution());
        ModuleAPI.addProvider(new ModuleProviderInfrared());
        ModuleAPI.addProvider(new ModuleProviderKeypad());
        ModuleAPI.addProvider(new ModuleProviderSerialPort());
        ModuleAPI.addProvider(new ModuleProviderStack());
        ModuleAPI.addProvider(new ModuleProviderRandom());
        ModuleAPI.addProvider(new ModuleProviderRandomAccessMemory());
        ModuleAPI.addProvider(new ModuleProviderRedstone());

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
        GameRegistry.registerTileEntity(tileEntity, name);

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

    public Item registerModule(final String name) {
        if (Settings.disabledModules.contains(name)) {
            return null;
        }

        return registerItem(name, ItemModule::new);
    }

    // --------------------------------------------------------------------- //

    private static void registerModuleOre(final String name) {
        if (Settings.disabledModules.contains(name)) {
            return;
        }

        OreDictionary.registerOre(API.MOD_ID + ":module", Item.REGISTRY.getObject(new ResourceLocation(API.MOD_ID, name)));
    }

}
