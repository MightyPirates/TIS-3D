package li.cil.tis3d.common;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.ModuleAPI;
import li.cil.tis3d.api.prefab.ResourceContentProvider;
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
import li.cil.tis3d.common.item.ItemBookCode;
import li.cil.tis3d.common.item.ItemBookManual;
import li.cil.tis3d.common.item.ItemModule;
import li.cil.tis3d.common.network.Network;
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
import net.minecraftforge.oredict.OreDictionary;

import java.util.function.Supplier;

/**
 * Takes care of common setup.
 */
public class ProxyCommon {
    private int controllerRenderId;
    private int casingRenderId;

    public int getControllerRenderId() {
        return controllerRenderId;
    }

    public void setControllerRenderId(final int controllerRenderId) {
        this.controllerRenderId = controllerRenderId;
    }

    public int getCasingRenderId() {
        return casingRenderId;
    }

    public void setCasingRenderId(final int casingRenderId) {
        this.casingRenderId = casingRenderId;
    }

    // --------------------------------------------------------------------- //

    public void onPreInit(final FMLPreInitializationEvent event) {
        // Initialize API.
        API.creativeTab = new CreativeTab();

        API.fontRendererAPI = new FontRendererAPIImpl();
        API.infraredAPI = new InfraredAPIImpl();
        API.manualAPI = ManualAPIImpl.INSTANCE;
        API.moduleAPI = new ModuleAPIImpl();

        // Register blocks and items.
        registerBlock(Constants.NAME_BLOCK_CASING, BlockCasing::new, TileEntityCasing.class);
        registerBlock(Constants.NAME_BLOCK_CONTROLLER, BlockController::new, TileEntityController.class);

        registerModule(Constants.NAME_ITEM_MODULE_EXECUTION);
        registerModule(Constants.NAME_ITEM_MODULE_INFRARED);
        registerModule(Constants.NAME_ITEM_MODULE_RANDOM);
        registerModule(Constants.NAME_ITEM_MODULE_REDSTONE);
        registerModule(Constants.NAME_ITEM_MODULE_STACK);

        registerItem(Constants.NAME_ITEM_BOOK_CODE, ItemBookCode::new);
        registerItem(Constants.NAME_ITEM_BOOK_MANUAL, ItemBookManual::new);

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
        FMLCommonHandler.instance().bus().register(TickHandlerInfraredPacket.INSTANCE);

        // Register providers for built-in modules.
        ModuleAPI.addProvider(new ModuleProviderExecution());
        ModuleAPI.addProvider(new ModuleProviderInfrared());
        ModuleAPI.addProvider(new ModuleProviderStack());
        ModuleAPI.addProvider(new ModuleProviderRandom());
        ModuleAPI.addProvider(new ModuleProviderRedstone());

        // Add default manual providers for server side stuff.
        ManualAPI.addProvider(new GameRegistryPathProvider());
        ManualAPI.addProvider(new ResourceContentProvider("tis3d", "doc/"));
    }

    // --------------------------------------------------------------------- //

    protected Block registerBlock(final String name, final Supplier<Block> constructor, final Class<? extends TileEntity> tileEntity) {
        final Block block = constructor.get().
                setBlockName(API.MOD_ID + "." + name).
                setBlockTextureName(API.MOD_ID + ":" + name).
                setCreativeTab(API.creativeTab);
        GameRegistry.registerBlock(block, name);
        GameRegistry.registerTileEntity(tileEntity, name);
        return block;
    }

    protected Item registerItem(final String name, final Supplier<Item> constructor) {
        final Item item = constructor.get().
                setUnlocalizedName(API.MOD_ID + "." + name).
                setTextureName(API.MOD_ID + ":" + name).
                setCreativeTab(API.creativeTab);
        GameRegistry.registerItem(item, name);
        return item;
    }

    protected Item registerModule(final String name) {
        return registerItem(name, ItemModule::new);
    }
}
