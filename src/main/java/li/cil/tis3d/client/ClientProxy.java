package li.cil.tis3d.client;

import li.cil.tis3d.Constants;
import li.cil.tis3d.common.CommonProxy;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Takes care of client-side only setup.
 */
public final class ClientProxy extends CommonProxy {
    @Override
    public void onPreInit(final FMLPreInitializationEvent event) {
        super.onPreInit(event);

        // Set up custom models for our blocks.

        OBJLoader.instance.addDomain(Constants.MOD_ID.toLowerCase());

        setCustomBlockModelResourceLocation(Constants.BlockCasingName);
        setCustomBlockModelResourceLocation(Constants.BlockControllerName);

        setCustomItemModelResourceLocation(Constants.ItemModuleExecutableName);
        setCustomItemModelResourceLocation(Constants.ItemModuleRedstoneName);
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
