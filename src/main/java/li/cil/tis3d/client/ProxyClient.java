package li.cil.tis3d.client;

import li.cil.tis3d.Constants;
import li.cil.tis3d.client.render.tile.TileEntitySpecialRendererCasing;
import li.cil.tis3d.common.ProxyCommon;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Takes care of client-side only setup.
 */
public final class ProxyClient extends ProxyCommon {
    @Override
    public void onPreInit(final FMLPreInitializationEvent event) {
        super.onPreInit(event);

        // Set up custom models for our blocks.
        OBJLoader.instance.addDomain(Constants.MOD_ID.toLowerCase());

        setCustomBlockModelResourceLocation(Constants.NAME_BLOCK_CASING);
        setCustomBlockModelResourceLocation(Constants.NAME_BLOCK_CONTROLLER);

        setCustomItemModelResourceLocation(Constants.NAME_ITEM_MODULE_EXECUTION);
        setCustomItemModelResourceLocation(Constants.NAME_ITEM_MODULE_REDSTONE);
        setCustomItemModelResourceLocation(Constants.NAME_ITEM_MODULE_STACK);
        setCustomItemModelResourceLocation(Constants.NAME_ITEM_MODULE_RANDOM);

        MinecraftForge.EVENT_BUS.register(TextureLoader.INSTANCE);
    }

    @Override
    public void onInit(final FMLInitializationEvent event) {
        super.onInit(event);

        // Set up tile entity renderer for dynamic module content.
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCasing.class, new TileEntitySpecialRendererCasing());
    }

    // --------------------------------------------------------------------- //

    private static void setCustomBlockModelResourceLocation(final String blockName) {
        final Item item = Item.getItemFromBlock(GameRegistry.findBlock(Constants.MOD_ID, blockName));
        final String path = Constants.MOD_ID.toLowerCase() + ":" + blockName;
        final ModelResourceLocation location = new ModelResourceLocation(path, "inventory");
        ModelLoader.setCustomModelResourceLocation(item, 0, location);
    }

    private static void setCustomItemModelResourceLocation(final String itemName) {
        final Item item = GameRegistry.findItem(Constants.MOD_ID, itemName);
        final String path = Constants.MOD_ID.toLowerCase() + ":" + itemName;
        final ModelResourceLocation location = new ModelResourceLocation(path, "inventory");
        ModelLoader.setCustomModelResourceLocation(item, 0, location);
    }
}
