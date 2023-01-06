package li.cil.tis3d.client;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.client.gui.ReadOnlyMemoryModuleScreen;
import li.cil.tis3d.client.renderer.block.entity.CasingBlockEntityRenderer;
import li.cil.tis3d.client.renderer.block.entity.ControllerBlockEntityRenderer;
import li.cil.tis3d.client.renderer.font.NormalFontRenderer;
import li.cil.tis3d.client.renderer.font.SmallFontRenderer;
import li.cil.tis3d.common.block.entity.BlockEntities;
import li.cil.tis3d.common.container.Containers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Takes care of client-side only setup.
 */
@Environment(EnvType.CLIENT)
public final class ClientSetup {
    public static void run() {
        API.normalFontRenderer = NormalFontRenderer.INSTANCE;
        API.smallFontRenderer = SmallFontRenderer.INSTANCE;

        MenuRegistry.registerScreenFactory(Containers.READ_ONLY_MEMORY_MODULE.get(), ReadOnlyMemoryModuleScreen::new);

        BlockEntityRendererRegistry.register(BlockEntities.CASING.get(), CasingBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(BlockEntities.CONTROLLER.get(), ControllerBlockEntityRenderer::new);

        ClientTickEvent.CLIENT_POST.register(level -> AbstractModule.MainThreadDisposer.disposeModules());
    }
}
