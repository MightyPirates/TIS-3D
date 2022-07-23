package li.cil.tis3d.client;

import li.cil.tis3d.api.API;
import li.cil.tis3d.client.gui.ReadOnlyMemoryModuleScreen;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.client.renderer.block.ModuleModelLoader;
import li.cil.tis3d.client.renderer.block.entity.CasingBlockEntityRenderer;
import li.cil.tis3d.client.renderer.block.entity.ControllerBlockEntityRenderer;
import li.cil.tis3d.client.renderer.color.CasingBlockColor;
import li.cil.tis3d.client.renderer.entity.NullEntityRenderer;
import li.cil.tis3d.client.renderer.font.NormalFontRenderer;
import li.cil.tis3d.client.renderer.font.SmallFontRenderer;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.block.entity.BlockEntities;
import li.cil.tis3d.common.container.Containers;
import li.cil.tis3d.common.entity.Entities;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
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

        BlockEntityRenderers.register(BlockEntities.CASING.get(), CasingBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntities.CONTROLLER.get(), ControllerBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void handleModelRegistryEvent(final ModelEvent.RegisterGeometryLoaders event) {
        event.register("module", new ModuleModelLoader());
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

    @SubscribeEvent
    public static void handleRegisterColorHandlersEvent(final RegisterColorHandlersEvent.Block event) {
        event.register(new CasingBlockColor(), Blocks.CASING.get());
    }
}
