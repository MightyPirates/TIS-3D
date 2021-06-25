package li.cil.tis3d.client;

import li.cil.tis3d.api.API;
import li.cil.tis3d.client.gui.ReadOnlyMemoryModuleScreen;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.client.renderer.block.ModuleModelLoader;
import li.cil.tis3d.client.renderer.entity.NullEntityRenderer;
import li.cil.tis3d.client.renderer.font.FontRendererNormal;
import li.cil.tis3d.client.renderer.font.FontRendererSmall;
import li.cil.tis3d.client.renderer.tileentity.TileEntitySpecialRendererCasing;
import li.cil.tis3d.client.renderer.tileentity.TileEntitySpecialRendererController;
import li.cil.tis3d.common.api.ManualAPIImpl;
import li.cil.tis3d.common.container.Containers;
import li.cil.tis3d.common.entity.Entities;
import li.cil.tis3d.common.module.ModuleDisplay;
import li.cil.tis3d.common.tileentity.TileEntities;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Objects;

/**
 * Takes care of client-side only setup.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent
    public static void handleSetupEvent(final FMLClientSetupEvent event) {
        API.normalFontRenderer = FontRendererNormal.INSTANCE;
        API.smallFontRenderer = FontRendererSmall.INSTANCE;
        API.manualAPI = ManualAPIImpl.INSTANCE;

        ScreenManager.register(Containers.READ_ONLY_MEMORY_MODULE.get(), ReadOnlyMemoryModuleScreen::new);

        ClientRegistry.bindTileEntityRenderer(TileEntities.CASING.get(), TileEntitySpecialRendererCasing::new);
        ClientRegistry.bindTileEntityRenderer(TileEntities.CONTROLLER.get(), TileEntitySpecialRendererController::new);

        RenderingRegistry.registerEntityRenderingHandler(Entities.INFRARED_PACKET.get(), NullEntityRenderer::new);

        MinecraftForge.EVENT_BUS.addListener(ModuleDisplay.TextureDisposer::tick);
    }

    @SubscribeEvent
    public static void handleModelRegistryEvent(final ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(API.MOD_ID, "module"), new ModuleModelLoader());
    }

    @SubscribeEvent
    public static void handleTextureStitchEvent(final TextureStitchEvent.Pre event) {
        if (Objects.equals(event.getMap().location(), PlayerContainer.BLOCK_ATLAS)) {
            Textures.handleTextureStitchEvent(event);
        }
    }
}
