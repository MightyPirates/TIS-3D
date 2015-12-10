package li.cil.tis3d.client;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.prefab.ItemStackTabIconRenderer;
import li.cil.tis3d.api.prefab.ResourceContentProvider;
import li.cil.tis3d.api.prefab.TextureTabIconRenderer;
import li.cil.tis3d.client.gui.GuiHandlerClient;
import li.cil.tis3d.client.manual.provider.BlockImageProvider;
import li.cil.tis3d.client.manual.provider.GameRegistryPathProvider;
import li.cil.tis3d.client.manual.provider.ItemImageProvider;
import li.cil.tis3d.client.manual.provider.OreDictImageProvider;
import li.cil.tis3d.client.manual.provider.TextureImageProvider;
import li.cil.tis3d.client.render.TextureLoader;
import li.cil.tis3d.client.render.tile.TileEntitySpecialRendererCasing;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.ProxyCommon;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Takes care of client-side only setup.
 */
public final class ProxyClient extends ProxyCommon {
    @Override
    public void onPreInit(final FMLPreInitializationEvent event) {
        super.onPreInit(event);

        // Set up custom models for our blocks.
        OBJLoader.instance.addDomain(API.MOD_ID.toLowerCase());

        setCustomBlockModelResourceLocation(Constants.NAME_BLOCK_CASING);
        setCustomBlockModelResourceLocation(Constants.NAME_BLOCK_CONTROLLER);

        setCustomItemModelResourceLocation(Constants.NAME_ITEM_MANUAL);
        setCustomItemModelResourceLocation(Constants.NAME_ITEM_MODULE_EXECUTION);
        setCustomItemModelResourceLocation(Constants.NAME_ITEM_MODULE_INFRARED);
        setCustomItemModelResourceLocation(Constants.NAME_ITEM_MODULE_RANDOM);
        setCustomItemModelResourceLocation(Constants.NAME_ITEM_MODULE_REDSTONE);
        setCustomItemModelResourceLocation(Constants.NAME_ITEM_MODULE_STACK);

        MinecraftForge.EVENT_BUS.register(TextureLoader.INSTANCE);
    }

    @Override
    public void onInit(final FMLInitializationEvent event) {
        super.onInit(event);

        // Set up tile entity renderer for dynamic module content.
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCasing.class, new TileEntitySpecialRendererCasing());

        // Register GUI handler for fancy GUIs in our almost GUI-less mod!
        NetworkRegistry.INSTANCE.registerGuiHandler(TIS3D.instance, new GuiHandlerClient());

        // Add default manual providers.
        ManualAPI.addProvider(new GameRegistryPathProvider());
        ManualAPI.addProvider(new ResourceContentProvider("tis3d", "doc/"));
        ManualAPI.addProvider("", new TextureImageProvider());
        ManualAPI.addProvider("item", new ItemImageProvider());
        ManualAPI.addProvider("block", new BlockImageProvider());
        ManualAPI.addProvider("oredict", new OreDictImageProvider());

        ManualAPI.addTab(new TextureTabIconRenderer(new ResourceLocation(API.MOD_ID, "textures/gui/manualHome.png")), "tis3d.manual.home", "%LANGUAGE%/index.md");
        ManualAPI.addTab(new ItemStackTabIconRenderer(new ItemStack(GameRegistry.findBlock(API.MOD_ID, Constants.NAME_BLOCK_CONTROLLER))), "tis3d.manual.blocks", "%LANGUAGE%/block/index.md");
        ManualAPI.addTab(new ItemStackTabIconRenderer(new ItemStack(GameRegistry.findItem(API.MOD_ID, Constants.NAME_ITEM_MODULE_EXECUTION))), "tis3d.manual.items", "%LANGUAGE%/item/index.md");
    }

    // --------------------------------------------------------------------- //

    private static void setCustomBlockModelResourceLocation(final String blockName) {
        final Item item = Item.getItemFromBlock(GameRegistry.findBlock(API.MOD_ID, blockName));
        final String path = API.MOD_ID.toLowerCase() + ":" + blockName;
        final ModelResourceLocation location = new ModelResourceLocation(path, "inventory");
        ModelLoader.setCustomModelResourceLocation(item, 0, location);
    }

    private static void setCustomItemModelResourceLocation(final String itemName) {
        final Item item = GameRegistry.findItem(API.MOD_ID, itemName);
        final String path = API.MOD_ID.toLowerCase() + ":" + itemName;
        final ModelResourceLocation location = new ModelResourceLocation(path, "inventory");
        ModelLoader.setCustomModelResourceLocation(item, 0, location);
    }
}
