package li.cil.tis3d.common;

import li.cil.tis3d.Constants;
import li.cil.tis3d.api.API;
import li.cil.tis3d.common.block.BlockCasing;
import li.cil.tis3d.common.block.BlockController;
import li.cil.tis3d.common.item.ItemModule;
import li.cil.tis3d.common.provider.ModuleProviderExecutable;
import li.cil.tis3d.common.provider.ModuleProviderRedstone;
import li.cil.tis3d.common.tile.TileEntityCasing;
import li.cil.tis3d.common.tile.TileEntityController;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Takes care of common setup.
 */
public class CommonProxy {
    public void onPreInit(final FMLPreInitializationEvent event) {
        // Register blocks and items.
        final CreativeTab creativeTab = new CreativeTab();

        GameRegistry.registerBlock(new BlockCasing().
                        setUnlocalizedName(Constants.BlockCasingName).
                        setCreativeTab(creativeTab),
                Constants.BlockCasingName);
        GameRegistry.registerBlock(new BlockController().
                        setUnlocalizedName(Constants.BlockControllerName).
                        setCreativeTab(creativeTab),
                Constants.BlockControllerName);

        GameRegistry.registerTileEntity(TileEntityCasing.class, Constants.BlockCasingName);
        GameRegistry.registerTileEntity(TileEntityController.class, Constants.BlockControllerName);

        GameRegistry.registerItem(new ItemModule().
                        setUnlocalizedName(Constants.ItemModuleExecutableName).
                        setCreativeTab(creativeTab),
                Constants.ItemModuleExecutableName);
        GameRegistry.registerItem(new ItemModule().
                        setUnlocalizedName(Constants.ItemModuleRedstoneName).
                        setCreativeTab(creativeTab),
                Constants.ItemModuleRedstoneName);

        // Initialize API.
        API.instance = new RegistryImpl();
    }

    public void onInit(final FMLInitializationEvent event) {
        // Register providers for built-in modules.
        API.addProvider(new ModuleProviderExecutable());
        API.addProvider(new ModuleProviderRedstone());
    }

    public void onPostInit(final FMLPostInitializationEvent event) {
    }
}
