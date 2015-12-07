package li.cil.tis3d.common;

import li.cil.tis3d.Constants;
import li.cil.tis3d.Settings;
import li.cil.tis3d.TIS3D;
import li.cil.tis3d.api.API;
import li.cil.tis3d.common.block.BlockCasing;
import li.cil.tis3d.common.block.BlockController;
import li.cil.tis3d.common.entity.EntityInfraredPacket;
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
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Takes care of common setup.
 */
public class ProxyCommon {
    public void onPreInit(final FMLPreInitializationEvent event) {
        // Initialize API.
        API.instance = new RegistryImpl();
        API.creativeTab = new CreativeTab();

        // Register blocks and items.
        GameRegistry.registerBlock(new BlockCasing().
                        setUnlocalizedName(Constants.NAME_BLOCK_CASING).
                        setCreativeTab(API.creativeTab),
                Constants.NAME_BLOCK_CASING);
        GameRegistry.registerBlock(new BlockController().
                        setUnlocalizedName(Constants.NAME_BLOCK_CONTROLLER).
                        setCreativeTab(API.creativeTab),
                Constants.NAME_BLOCK_CONTROLLER);

        GameRegistry.registerTileEntity(TileEntityCasing.class, Constants.NAME_BLOCK_CASING);
        GameRegistry.registerTileEntity(TileEntityController.class, Constants.NAME_BLOCK_CONTROLLER);

        GameRegistry.registerItem(new ItemModule().
                        setUnlocalizedName(Constants.NAME_ITEM_MODULE_EXECUTION).
                        setCreativeTab(API.creativeTab),
                Constants.NAME_ITEM_MODULE_EXECUTION);
        GameRegistry.registerItem(new ItemModule().
                        setUnlocalizedName(Constants.NAME_ITEM_MODULE_INFRARED).
                        setCreativeTab(API.creativeTab),
                Constants.NAME_ITEM_MODULE_INFRARED);
        GameRegistry.registerItem(new ItemModule().
                        setUnlocalizedName(Constants.NAME_ITEM_MODULE_RANDOM).
                        setCreativeTab(API.creativeTab),
                Constants.NAME_ITEM_MODULE_RANDOM);
        GameRegistry.registerItem(new ItemModule().
                        setUnlocalizedName(Constants.NAME_ITEM_MODULE_REDSTONE).
                        setCreativeTab(API.creativeTab),
                Constants.NAME_ITEM_MODULE_REDSTONE);
        GameRegistry.registerItem(new ItemModule().
                        setUnlocalizedName(Constants.NAME_ITEM_MODULE_STACK).
                        setCreativeTab(API.creativeTab),
                Constants.NAME_ITEM_MODULE_STACK);

        Settings.load(event.getSuggestedConfigurationFile());
    }

    public void onInit(final FMLInitializationEvent event) {
        // Hardcoded recipes!
        GameRegistry.addRecipe(new ItemStack(GameRegistry.findBlock(API.MOD_ID, Constants.NAME_BLOCK_CASING), 8),
                "SRS",
                "RIR",
                "SRS",
                'S', Blocks.stone,
                'R', Items.redstone,
                'I', Blocks.iron_block);
        GameRegistry.addRecipe(new ItemStack(GameRegistry.findBlock(API.MOD_ID, Constants.NAME_BLOCK_CONTROLLER), 1),
                "SRS",
                "RDR",
                "SRS",
                'S', Blocks.stone,
                'R', Items.redstone,
                'D', Items.diamond);

        GameRegistry.addRecipe(new ItemStack(GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_EXECUTION), 2),
                "PPP",
                "RGR",
                'P', Blocks.glass_pane,
                'R', Items.redstone,
                'G', Items.gold_ingot);
        GameRegistry.addRecipe(new ItemStack(GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_INFRARED), 2),
                "PPP",
                "RGR",
                'P', Blocks.glass_pane,
                'R', Items.redstone,
                'G', Items.spider_eye);
        GameRegistry.addRecipe(new ItemStack(GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_RANDOM), 2),
                "PPP",
                "RER",
                'P', Blocks.glass_pane,
                'R', Items.redstone,
                'E', Items.ender_pearl);
        GameRegistry.addRecipe(new ItemStack(GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_REDSTONE), 2),
                "PPP",
                "RIR",
                'P', Blocks.glass_pane,
                'R', Items.redstone,
                'I', Items.repeater);
        GameRegistry.addRecipe(new ItemStack(GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_STACK), 2),
                "PPP",
                "RER",
                'P', Blocks.glass_pane,
                'R', Items.redstone,
                'E', Blocks.chest);

        // Register entities.
        EntityRegistry.registerModEntity(EntityInfraredPacket.class, Constants.NAME_ENTITY_INFRARED_PACKET, 1, TIS3D.instance, 16, 1, true);

        // Register network handler.
        Network.INSTANCE.init();

        // Register providers for built-in modules.
        API.addProvider(new ModuleProviderExecution());
        API.addProvider(new ModuleProviderInfrared());
        API.addProvider(new ModuleProviderStack());
        API.addProvider(new ModuleProviderRandom());
        API.addProvider(new ModuleProviderRedstone());
    }
}
