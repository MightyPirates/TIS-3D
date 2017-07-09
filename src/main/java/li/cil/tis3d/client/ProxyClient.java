package li.cil.tis3d.client;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.prefab.client.SimpleModuleRenderer;
import li.cil.tis3d.api.prefab.manual.ItemStackTabIconRenderer;
import li.cil.tis3d.api.prefab.manual.TextureTabIconRenderer;
import li.cil.tis3d.client.gui.GuiHandlerClient;
import li.cil.tis3d.client.manual.provider.BlockImageProvider;
import li.cil.tis3d.client.manual.provider.ItemImageProvider;
import li.cil.tis3d.client.manual.provider.OreDictImageProvider;
import li.cil.tis3d.client.manual.provider.TextureImageProvider;
import li.cil.tis3d.client.renderer.TextureLoader;
import li.cil.tis3d.client.renderer.block.ISBRHCasing;
import li.cil.tis3d.client.renderer.block.ISBRHController;
import li.cil.tis3d.client.renderer.tileentity.TileEntitySpecialRendererCasing;
import li.cil.tis3d.client.renderer.tileentity.TileEntitySpecialRendererController;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.ProxyCommon;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.init.Blocks;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import li.cil.tis3d.common.tileentity.TileEntityController;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collection;

/**
 * Takes care of client-side only setup.
 */
public final class ProxyClient extends ProxyCommon {
    @Override
    public void onPreInit(final FMLPreInitializationEvent event) {
        super.onPreInit(event);

        // Set up OBJ loader for this mod.
        RenderingRegistry.registerBlockHandler(new ISBRHCasing());
        RenderingRegistry.registerBlockHandler(new ISBRHController());

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

        ManualAPI.addTab(new TextureTabIconRenderer(new ResourceLocation(API.MOD_ID, "textures/gui/manualHome.png")), "tis3d.manual.home", "%LANGUAGE%/index.md");
        ManualAPI.addTab(new ItemStackTabIconRenderer(new ItemStack(Blocks.controller)), "tis3d.manual.blocks", "%LANGUAGE%/block/index.md");
        final Item module;
        if (Items.getModules().containsKey(Constants.NAME_ITEM_MODULE_EXECUTION)) {
            module = Items.getModules().get(Constants.NAME_ITEM_MODULE_EXECUTION);
        } else {
            final Collection<Item> allModules = Items.getModules().values();
            if (allModules.isEmpty()) {
                module = net.minecraft.init.Items.redstone;
            } else {
                module = allModules.iterator().next();
            }
        }
        ManualAPI.addTab(new ItemStackTabIconRenderer(new ItemStack(module)), "tis3d.manual.items", "%LANGUAGE%/item/index.md");
        ManualAPI.addTab(new TextureTabIconRenderer(new ResourceLocation(API.MOD_ID, "textures/gui/manualSerialProtocols.png")), "tis3d.manual.serialProtocols", "%LANGUAGE%/serialProtocols.md");
    }

    // --------------------------------------------------------------------- //

    @Override
    public Item registerModule(final String name) {
        final Item item = super.registerModule(name);
        MinecraftForgeClient.registerItemRenderer(item, new SimpleModuleRenderer().setIgnoreLighting(true));
        return item;
    }
}
