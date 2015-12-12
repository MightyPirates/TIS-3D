package li.cil.tis3d.common;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.ModuleAPI;
import li.cil.tis3d.common.api.CreativeTab;
import li.cil.tis3d.common.api.FontRendererAPIImpl;
import li.cil.tis3d.common.api.InfraredAPIImpl;
import li.cil.tis3d.common.api.ManualAPIImpl;
import li.cil.tis3d.common.api.ModuleAPIImpl;
import li.cil.tis3d.common.block.BlockCasing;
import li.cil.tis3d.common.block.BlockController;
import li.cil.tis3d.common.entity.EntityInfraredPacket;
import li.cil.tis3d.common.event.TickHandlerInfraredPacket;
import li.cil.tis3d.common.item.ItemBookManual;
import li.cil.tis3d.common.item.ItemBookCode;
import li.cil.tis3d.common.item.ItemModule;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.provider.ModuleProviderExecution;
import li.cil.tis3d.common.provider.ModuleProviderInfrared;
import li.cil.tis3d.common.provider.ModuleProviderRandom;
import li.cil.tis3d.common.provider.ModuleProviderRedstone;
import li.cil.tis3d.common.provider.ModuleProviderStack;
import li.cil.tis3d.common.tile.TileEntityCasing;
import li.cil.tis3d.common.tile.TileEntityController;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Takes care of common setup.
 */
public class ProxyCommon {
    public void onPreInit(final FMLPreInitializationEvent event) {
        // Initialize API.
        API.creativeTab = new CreativeTab();

        API.fontRendererAPI = new FontRendererAPIImpl();
        API.infraredAPI = new InfraredAPIImpl();
        API.manualAPI = ManualAPIImpl.INSTANCE;
        API.moduleAPI = new ModuleAPIImpl();

        // Register blocks and items.
        GameRegistry.registerBlock(new BlockCasing().
                        setUnlocalizedName(API.MOD_ID + "." + Constants.NAME_BLOCK_CASING).
                        setCreativeTab(API.creativeTab),
                Constants.NAME_BLOCK_CASING);
        GameRegistry.registerBlock(new BlockController().
                        setUnlocalizedName(API.MOD_ID + "." + Constants.NAME_BLOCK_CONTROLLER).
                        setCreativeTab(API.creativeTab),
                Constants.NAME_BLOCK_CONTROLLER);

        GameRegistry.registerTileEntity(TileEntityCasing.class, Constants.NAME_BLOCK_CASING);
        GameRegistry.registerTileEntity(TileEntityController.class, Constants.NAME_BLOCK_CONTROLLER);

        GameRegistry.registerItem(new ItemModule().
                        setUnlocalizedName(API.MOD_ID + "." + Constants.NAME_ITEM_MODULE_EXECUTION).
                        setCreativeTab(API.creativeTab),
                Constants.NAME_ITEM_MODULE_EXECUTION);
        GameRegistry.registerItem(new ItemModule().
                        setUnlocalizedName(API.MOD_ID + "." + Constants.NAME_ITEM_MODULE_INFRARED).
                        setCreativeTab(API.creativeTab),
                Constants.NAME_ITEM_MODULE_INFRARED);
        GameRegistry.registerItem(new ItemModule().
                        setUnlocalizedName(API.MOD_ID + "." + Constants.NAME_ITEM_MODULE_RANDOM).
                        setCreativeTab(API.creativeTab),
                Constants.NAME_ITEM_MODULE_RANDOM);
        GameRegistry.registerItem(new ItemModule().
                        setUnlocalizedName(API.MOD_ID + "." + Constants.NAME_ITEM_MODULE_REDSTONE).
                        setCreativeTab(API.creativeTab),
                Constants.NAME_ITEM_MODULE_REDSTONE);
        GameRegistry.registerItem(new ItemModule().
                        setUnlocalizedName(API.MOD_ID + "." + Constants.NAME_ITEM_MODULE_STACK).
                        setCreativeTab(API.creativeTab),
                Constants.NAME_ITEM_MODULE_STACK);

        GameRegistry.registerItem(new ItemBookCode().
                        setUnlocalizedName(API.MOD_ID + "." +Constants.NAME_ITEM_BOOK_CODE).
                        setCreativeTab(API.creativeTab),
                Constants.NAME_ITEM_BOOK_CODE);
        GameRegistry.registerItem(new ItemBookManual().
                        setUnlocalizedName(API.MOD_ID + "." + Constants.NAME_ITEM_BOOK_MANUAL).
                        setCreativeTab(API.creativeTab),
                Constants.NAME_ITEM_BOOK_MANUAL);

        Settings.load(event.getSuggestedConfigurationFile());
    }

    public void onInit(final FMLInitializationEvent event) {
        // Register Ore Dictionary entries.
        OreDictionary.registerOre("book", GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_BOOK_CODE));
        OreDictionary.registerOre("book", GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_BOOK_MANUAL));

        OreDictionary.registerOre(API.MOD_ID + ":module", GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_EXECUTION));
        OreDictionary.registerOre(API.MOD_ID + ":module", GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_INFRARED));
        OreDictionary.registerOre(API.MOD_ID + ":module", GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_RANDOM));
        OreDictionary.registerOre(API.MOD_ID + ":module", GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_REDSTONE));
        OreDictionary.registerOre(API.MOD_ID + ":module", GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_STACK));

        // Hardcoded recipes!
        GameRegistry.addRecipe(new ItemStack(GameRegistry.findBlock(API.MOD_ID, Constants.NAME_BLOCK_CASING), 8),
                "IRI",
                "RBR",
                "IRI",
                'I', Items.iron_ingot,
                'R', Items.redstone,
                'B', Blocks.iron_block);
        GameRegistry.addRecipe(new ItemStack(GameRegistry.findBlock(API.MOD_ID, Constants.NAME_BLOCK_CONTROLLER), 1),
                "IRI",
                "RDR",
                "IRI",
                'I', Items.iron_ingot,
                'R', Items.redstone,
                'D', Items.diamond);

        GameRegistry.addRecipe(new ItemStack(GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_EXECUTION), 2),
                "PPP",
                "IGI",
                " R ",
                'P', Blocks.glass_pane,
                'I', Items.iron_ingot,
                'R', Items.redstone,
                'G', Items.gold_ingot);
        GameRegistry.addRecipe(new ItemStack(GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_INFRARED), 2),
                "PPP",
                "IGI",
                " R ",
                'P', Blocks.glass_pane,
                'I', Items.iron_ingot,
                'R', Items.redstone,
                'G', Items.spider_eye);
        GameRegistry.addRecipe(new ItemStack(GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_RANDOM), 2),
                "PPP",
                "IEI",
                " R ",
                'P', Blocks.glass_pane,
                'I', Items.iron_ingot,
                'R', Items.redstone,
                'E', Items.ender_pearl);
        GameRegistry.addRecipe(new ItemStack(GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_REDSTONE), 2),
                "PPP",
                "ICI",
                " R ",
                'P', Blocks.glass_pane,
                'I', Items.iron_ingot,
                'R', Items.redstone,
                'C', Items.repeater);
        GameRegistry.addRecipe(new ItemStack(GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_STACK), 2),
                "PPP",
                "IEI",
                " R ",
                'P', Blocks.glass_pane,
                'I', Items.iron_ingot,
                'R', Items.redstone,
                'E', Blocks.chest);

        // Register entities.
        EntityRegistry.registerModEntity(EntityInfraredPacket.class, Constants.NAME_ENTITY_INFRARED_PACKET, 1, TIS3D.instance, 16, 1, true);

        // Register network handler.
        Network.INSTANCE.init();

        // Register event handlers.
        MinecraftForge.EVENT_BUS.register(TickHandlerInfraredPacket.INSTANCE);

        // Register providers for built-in modules.
        ModuleAPI.addProvider(new ModuleProviderExecution());
        ModuleAPI.addProvider(new ModuleProviderInfrared());
        ModuleAPI.addProvider(new ModuleProviderStack());
        ModuleAPI.addProvider(new ModuleProviderRandom());
        ModuleAPI.addProvider(new ModuleProviderRedstone());
    }
}
