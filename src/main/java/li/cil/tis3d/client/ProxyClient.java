package li.cil.tis3d.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import li.cil.tis3d.client.render.block.ISBRHCasing;
import li.cil.tis3d.client.render.block.ISBRHController;
import li.cil.tis3d.client.render.tile.TileEntitySpecialRendererCasing;
import li.cil.tis3d.common.ProxyCommon;
import li.cil.tis3d.common.tile.TileEntityCasing;
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
    }
}
