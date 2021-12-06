package li.cil.tis3d.client;

import li.cil.tis3d.api.API;
import li.cil.tis3d.client.gui.ReadOnlyMemoryModuleScreen;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.client.renderer.block.ModuleModelLoader;
import li.cil.tis3d.client.renderer.color.CasingBlockColor;
import li.cil.tis3d.client.renderer.entity.NullEntityRenderer;
import li.cil.tis3d.client.renderer.font.NormalFontRenderer;
import li.cil.tis3d.client.renderer.font.SmallFontRenderer;
import li.cil.tis3d.client.renderer.tileentity.CasingTileEntityRenderer;
import li.cil.tis3d.client.renderer.tileentity.ControllerTileEntityRenderer;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.container.Containers;
import li.cil.tis3d.common.entity.Entities;
import li.cil.tis3d.common.module.DisplayModule;
import li.cil.tis3d.common.tileentity.TileEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Objects;

/**
 * Takes care of client-side only setup.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent
    public static void handleSetupEvent(final FMLClientSetupEvent event) {
        API.normalFontRenderer = NormalFontRenderer.INSTANCE;
        API.smallFontRenderer = SmallFontRenderer.INSTANCE;

        MenuScreens.register(Containers.READ_ONLY_MEMORY_MODULE.get(), ReadOnlyMemoryModuleScreen::new);

        BlockEntityRenderers.register(TileEntities.CASING.get(), CasingTileEntityRenderer::new);
        BlockEntityRenderers.register(TileEntities.CONTROLLER.get(), ControllerTileEntityRenderer::new);

        MinecraftForge.EVENT_BUS.addListener(DisplayModule.TextureDisposer::tick);

        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(Blocks.CASING.get(), (RenderType) -> true);
            Minecraft.getInstance().getBlockColors().register(new CasingBlockColor(), Blocks.CASING.get());
        });
    }

    @SubscribeEvent
    public static void handleModelRegistryEvent(final ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(API.MOD_ID, "module"), new ModuleModelLoader());
    }

    @SubscribeEvent
    public static void handleTextureStitchEvent(final TextureStitchEvent.Pre event) {
        if (Objects.equals(event.getAtlas().location(), InventoryMenu.BLOCK_ATLAS)) {
            Textures.handleTextureStitchEvent(event);
        }
    }

    @SubscribeEvent
    public static void handleEntityRendererRegisterEvent(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Entities.INFRARED_PACKET.get(), NullEntityRenderer::new);
    }
}
