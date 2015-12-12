package li.cil.tis3d.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.prefab.ItemStackTabIconRenderer;
import li.cil.tis3d.api.prefab.TextureTabIconRenderer;
import li.cil.tis3d.client.gui.GuiHandlerClient;
import li.cil.tis3d.client.manual.provider.BlockImageProvider;
import li.cil.tis3d.client.manual.provider.ItemImageProvider;
import li.cil.tis3d.client.manual.provider.OreDictImageProvider;
import li.cil.tis3d.client.manual.provider.TextureImageProvider;
import li.cil.tis3d.client.render.TextureLoader;
import li.cil.tis3d.client.render.block.ISBRHCasing;
import li.cil.tis3d.client.render.block.ISBRHController;
import li.cil.tis3d.api.prefab.client.SimpleModuleRenderer;
import li.cil.tis3d.client.render.tile.TileEntitySpecialRendererCasing;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.ProxyCommon;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

/**
 * Takes care of client-side only setup.
 */
public final class ProxyClient extends ProxyCommon {
    @Override
    public void onPreInit(final FMLPreInitializationEvent event) {
        super.onPreInit(event);

        // Set up custom models for our blocks.
        RenderingRegistry.registerBlockHandler(new ISBRHCasing());
        RenderingRegistry.registerBlockHandler(new ISBRHController());

        MinecraftForge.EVENT_BUS.register(TextureLoader.INSTANCE);
    }

	@Override
    public void onInit(final FMLInitializationEvent event) {
        super.onInit(event);

        // Set up tile entity renderer for dynamic module content.
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCasing.class, new TileEntitySpecialRendererCasing());

        // Register GUI handler for fancy GUIs in our almost GUI-less mod!
        NetworkRegistry.INSTANCE.registerGuiHandler(TIS3D.instance, new GuiHandlerClient());

        // Add default manual providers for client side stuff.
        ManualAPI.addProvider("", new TextureImageProvider());
        ManualAPI.addProvider("item", new ItemImageProvider());
        ManualAPI.addProvider("block", new BlockImageProvider());
        ManualAPI.addProvider("oredict", new OreDictImageProvider());

        ManualAPI.addTab(new TextureTabIconRenderer(new ResourceLocation(API.MOD_ID, "textures/gui/manualHome.png")), "tis3d.manual.home", "%LANGUAGE%/index.md");
        ManualAPI.addTab(new ItemStackTabIconRenderer(new ItemStack(GameRegistry.findBlock(API.MOD_ID, Constants.NAME_BLOCK_CONTROLLER))), "tis3d.manual.blocks", "%LANGUAGE%/block/index.md");
        ManualAPI.addTab(new ItemStackTabIconRenderer(new ItemStack(GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_EXECUTION))), "tis3d.manual.items", "%LANGUAGE%/item/index.md");
    }


	@Override
	protected Item registerModule(String name) {
		Item item = super.registerModule(name);
		MinecraftForgeClient.registerItemRenderer(item, getSimpleModuleRenderer());
		return item;
	}

	private IItemRenderer simpleModuleRenderer;

	private IItemRenderer getSimpleModuleRenderer() {
		if(simpleModuleRenderer == null) {
			simpleModuleRenderer = new SimpleModuleRenderer();
		}
		return simpleModuleRenderer;
	}
}
