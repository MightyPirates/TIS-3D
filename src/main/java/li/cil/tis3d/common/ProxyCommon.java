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
import li.cil.tis3d.common.block.BlockCasing;
import li.cil.tis3d.common.block.BlockController;
import li.cil.tis3d.common.entity.EntityInfraredPacket;
import li.cil.tis3d.common.event.TickHandlerInfraredPacket;
import li.cil.tis3d.common.integration.Integration;
import li.cil.tis3d.common.integration.redstone.RedstoneIntegration;
import li.cil.tis3d.common.item.ItemBookCode;
import li.cil.tis3d.common.item.ItemBookManual;
import li.cil.tis3d.common.item.ItemModule;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.provider.ModuleProviderAudio;
import li.cil.tis3d.common.provider.ModuleProviderBundledRedstone;
import li.cil.tis3d.common.provider.ModuleProviderDisplay;
import li.cil.tis3d.common.provider.ModuleProviderExecution;
import li.cil.tis3d.common.provider.ModuleProviderInfrared;
import li.cil.tis3d.common.provider.ModuleProviderRandom;
import li.cil.tis3d.common.provider.ModuleProviderRedstone;
import li.cil.tis3d.common.provider.ModuleProviderStack;
import li.cil.tis3d.common.tile.TileEntityCasing;
import li.cil.tis3d.common.tile.TileEntityController;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

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

        // Register blocks and items.
        registerBlock(Constants.NAME_BLOCK_CASING, BlockCasing::new, TileEntityCasing.class);
        registerBlock(Constants.NAME_BLOCK_CONTROLLER, BlockController::new, TileEntityController.class);

        registerModule(Constants.NAME_ITEM_MODULE_AUDIO);
        registerModule(Constants.NAME_ITEM_MODULE_BUNDLED_REDSTONE);
        registerModule(Constants.NAME_ITEM_MODULE_DISPLAY);
        registerModule(Constants.NAME_ITEM_MODULE_EXECUTION);
        registerModule(Constants.NAME_ITEM_MODULE_INFRARED);
        registerModule(Constants.NAME_ITEM_MODULE_RANDOM);
        registerModule(Constants.NAME_ITEM_MODULE_REDSTONE);
        registerModule(Constants.NAME_ITEM_MODULE_STACK);

        registerItem(Constants.NAME_ITEM_BOOK_CODE, ItemBookCode::new);
        registerItem(Constants.NAME_ITEM_BOOK_MANUAL, ItemBookManual::new);

        registerItem(Constants.NAME_ITEM_PRISM, Item::new);

        // Mod integration.
        Integration.INSTANCE.preInit(event);
    }

    public void onInit(final FMLInitializationEvent event) {
        // Register Ore Dictionary entries.
        OreDictionary.registerOre("book", GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_BOOK_CODE));
        OreDictionary.registerOre("book", GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_BOOK_MANUAL));

        registerModuleOre(Constants.NAME_ITEM_MODULE_AUDIO);
        registerModuleOre(Constants.NAME_ITEM_MODULE_BUNDLED_REDSTONE);
        registerModuleOre(Constants.NAME_ITEM_MODULE_DISPLAY);
        registerModuleOre(Constants.NAME_ITEM_MODULE_EXECUTION);
        registerModuleOre(Constants.NAME_ITEM_MODULE_INFRARED);
        registerModuleOre(Constants.NAME_ITEM_MODULE_RANDOM);
        registerModuleOre(Constants.NAME_ITEM_MODULE_REDSTONE);
        registerModuleOre(Constants.NAME_ITEM_MODULE_STACK);

        // Hardcoded recipes!
        addBlockRecipe(Constants.NAME_BLOCK_CASING, "blockIron", 8);
        addBlockRecipe(Constants.NAME_BLOCK_CONTROLLER, "gemDiamond", 1);

        addModuleRecipe(Constants.NAME_ITEM_MODULE_AUDIO, Item.getItemFromBlock(Blocks.noteblock));
        addModuleRecipe(Constants.NAME_ITEM_MODULE_BUNDLED_REDSTONE, Items.comparator);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_DISPLAY, GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_PRISM));
        addModuleRecipe(Constants.NAME_ITEM_MODULE_EXECUTION, "ingotGold");
        addModuleRecipe(Constants.NAME_ITEM_MODULE_INFRARED, Items.spider_eye);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_RANDOM, Items.ender_pearl);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_REDSTONE, Items.repeater);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_STACK, Item.getItemFromBlock(Blocks.chest));

        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_PRISM), 1),
                "gemQuartz",
                "dustRedstone",
                "gemLapis",
                "gemEmerald"));

        // Register entities.
        EntityRegistry.registerModEntity(EntityInfraredPacket.class, Constants.NAME_ENTITY_INFRARED_PACKET, 1, TIS3D.instance, 16, 1, true);

        // Register network handler.
        Network.INSTANCE.init();

        // Register event handlers.
        MinecraftForge.EVENT_BUS.register(RedstoneIntegration.INSTANCE);
        MinecraftForge.EVENT_BUS.register(TickHandlerInfraredPacket.INSTANCE);

        // Register providers for built-in modules.
        ModuleAPI.addProvider(new ModuleProviderAudio());
        ModuleAPI.addProvider(new ModuleProviderBundledRedstone());
        ModuleAPI.addProvider(new ModuleProviderDisplay());
        ModuleAPI.addProvider(new ModuleProviderExecution());
        ModuleAPI.addProvider(new ModuleProviderInfrared());
        ModuleAPI.addProvider(new ModuleProviderStack());
        ModuleAPI.addProvider(new ModuleProviderRandom());
        ModuleAPI.addProvider(new ModuleProviderRedstone());

        // Add default manual providers for server side stuff.
        ManualAPI.addProvider(new GameRegistryPathProvider());
        ManualAPI.addProvider(new ResourceContentProvider("tis3d", "doc/"));

        // Mod integration.
        Integration.INSTANCE.init(event);
    }

    public void onPostInit(final FMLPostInitializationEvent event) {
        // Mod integration.
        Integration.INSTANCE.postInit(event);
    }

    // --------------------------------------------------------------------- //

    protected Block registerBlock(final String name, final Supplier<Block> constructor, final Class<? extends TileEntity> tileEntity) {
        final Block block = constructor.get().
                setUnlocalizedName(API.MOD_ID + "." + name).
                setCreativeTab(API.creativeTab);
        GameRegistry.registerBlock(block, name);
        GameRegistry.registerTileEntity(tileEntity, name);
        return block;
    }

    protected Item registerItem(final String name, final Supplier<Item> constructor) {
        final Item item = constructor.get().
                setUnlocalizedName(API.MOD_ID + "." + name).
                setCreativeTab(API.creativeTab);
        GameRegistry.registerItem(item, name);
        return item;
    }

    protected Item registerModule(final String name) {
        if (Settings.disabledModules.contains(name)) {
            return null;
        }

        return registerItem(name, ItemModule::new);
    }

    private static void registerModuleOre(final String name) {
        if (Settings.disabledModules.contains(name)) {
            return;
        }

        OreDictionary.registerOre(API.MOD_ID + ":module", GameRegistry.findItem(API.MOD_ID, name));
    }

    private static void addBlockRecipe(final String name, final Object specialIngredient, final int count) {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(GameRegistry.findBlock(API.MOD_ID, name), count),
                "IRI",
                "RSR",
                "IRI",
                'I', "ingotIron",
                'R', "dustRedstone",
                'S', specialIngredient));
    }

    private static void addModuleRecipe(final String name, final Object specialIngredient) {
        if (Settings.disabledModules.contains(name)) {
            return;
        }

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(GameRegistry.findItem(API.MOD_ID, name), 2),
                "PPP",
                "ISI",
                " R ",
                'P', "paneGlassColorless",
                'I', "ingotIron",
                'R', "dustRedstone",
                'S', specialIngredient));
    }
}
