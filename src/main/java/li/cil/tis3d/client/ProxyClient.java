package li.cil.tis3d.client;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.prefab.manual.ItemStackTabIconRenderer;
import li.cil.tis3d.api.prefab.manual.TextureTabIconRenderer;
import li.cil.tis3d.client.manual.provider.BlockImageProvider;
import li.cil.tis3d.client.manual.provider.ItemImageProvider;
import li.cil.tis3d.client.manual.provider.TextureImageProvider;
import li.cil.tis3d.client.renderer.tileentity.TileEntitySpecialRendererCasing;
import li.cil.tis3d.client.renderer.tileentity.TileEntitySpecialRendererController;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.ProxyCommon;
import li.cil.tis3d.common.init.Blocks;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import li.cil.tis3d.common.tileentity.TileEntityController;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.dimdev.rift.listener.MinecraftStartListener;
import org.dimdev.rift.listener.client.TileEntityRendererAdder;
import pl.asie.protocharset.rift.network.PacketAdderClient;
import pl.asie.protocharset.rift.network.PacketRegistry;

import java.util.Collection;
import java.util.Map;

/**
 * Takes care of client-side only setup.
 */
//@Mod.EventBusSubscriber(Side.CLIENT)
// TODO Rift: MinecraftStartListener -> Client/ServerSideStarts
public final class ProxyClient extends ProxyCommon implements PacketAdderClient, TileEntityRendererAdder {
	@Override
	public void registerClientPackets(PacketRegistry packetRegistry) {
		ManualAPI.addProvider("", new TextureImageProvider());
		ManualAPI.addProvider("item", new ItemImageProvider());
		ManualAPI.addProvider("block", new BlockImageProvider());
		// TODO: TagImageProvider
		// ManualAPI.addProvider("oredict", new OreDictImageProvider());

		ManualAPI.addTab(new TextureTabIconRenderer(new ResourceLocation(API.MOD_ID, "textures/gui/manual_home.png")), "tis3d.manual.home", "%LANGUAGE%/index.md");
		ManualAPI.addTab(new ItemStackTabIconRenderer(new ItemStack(Blocks.controller)), "tis3d.manual.blocks", "%LANGUAGE%/block/index.md");
		final Item module;
		if (Items.getModules().containsKey(Constants.NAME_ITEM_MODULE_EXECUTION)) {
			module = Items.getModules().get(Constants.NAME_ITEM_MODULE_EXECUTION);
		} else {
			final Collection<Item> allModules = Items.getModules().values();
			if (allModules.isEmpty()) {
				module = net.minecraft.init.Items.REDSTONE;
			} else {
				module = allModules.iterator().next();
			}
		}
		ManualAPI.addTab(new ItemStackTabIconRenderer(new ItemStack(module)), "tis3d.manual.items", "%LANGUAGE%/item/index.md");
		ManualAPI.addTab(new TextureTabIconRenderer(new ResourceLocation(API.MOD_ID, "textures/gui/manual_serial_protocols.png")), "tis3d.manual.serialProtocols", "%LANGUAGE%/serial_protocols.md");
	}

	@Override
	public void addTileEntityRenderers(Map<Class<? extends TileEntity>, TileEntityRenderer<? extends TileEntity>> renderers) {
		renderers.put(TileEntityCasing.class, new TileEntitySpecialRendererCasing());
		renderers.put(TileEntityController.class, new TileEntitySpecialRendererController());
	}

	// TODO
    /* @Override
    public void onPreInit(final FMLPreInitializationEvent event) {
        super.onPreInit(event);

        MinecraftForge.EVENT_BUS.register(TextureLoader.INSTANCE);
    }

    @Override
    public void onInit(final FMLInitializationEvent event) {
        super.onInit(event);

        // Set up tile entity renderer for dynamic module content.
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCasing.class, new TileEntitySpecialRendererCasing());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityController.class, new TileEntitySpecialRendererController());

        // Register GUI handler for fancy GUIs in our almost GUI-less mod!
        NetworkRegistry.INSTANCE.registerGuiHandler(TIS3D.instance, new GuiHandlerClient());

        // Add default manual providers for client side stuff.
        ManualAPI.addProvider("", new TextureImageProvider());
        ManualAPI.addProvider("item", new ItemImageProvider());
        ManualAPI.addProvider("block", new BlockImageProvider());
        ManualAPI.addProvider("oredict", new OreDictImageProvider());

        ManualAPI.addTab(new TextureTabIconRenderer(new ResourceLocation(API.MOD_ID, "textures/gui/manual_home.png")), "tis3d.manual.home", "%LANGUAGE%/index.md");
        ManualAPI.addTab(new ItemStackTabIconRenderer(new ItemStack(Blocks.controller)), "tis3d.manual.blocks", "%LANGUAGE%/block/index.md");
        final Item module;
        if (Items.getModules().containsKey(Constants.NAME_ITEM_MODULE_EXECUTION)) {
            module = Items.getModules().get(Constants.NAME_ITEM_MODULE_EXECUTION);
        } else {
            final Collection<Item> allModules = Items.getModules().values();
            if (allModules.isEmpty()) {
                module = net.minecraft.init.Items.REDSTONE;
            } else {
                module = allModules.iterator().next();
            }
        }
        ManualAPI.addTab(new ItemStackTabIconRenderer(new ItemStack(module)), "tis3d.manual.items", "%LANGUAGE%/item/index.md");
        ManualAPI.addTab(new TextureTabIconRenderer(new ResourceLocation(API.MOD_ID, "textures/gui/manual_serial_protocols.png")), "tis3d.manual.serialProtocols", "%LANGUAGE%/serial_protocols.md");
    }

    // --------------------------------------------------------------------- //

    @SubscribeEvent
    public static void handleModelRegistryEvent(final ModelRegistryEvent event) {
        for (final Item item : Items.getAllItems()) {
            final ResourceLocation registryName = item.getRegistryName();
            assert registryName != null;
            final ModelResourceLocation location = new ModelResourceLocation(registryName, "inventory");
            ModelLoader.setCustomModelResourceLocation(item, 0, location);
        }
    } */
}
