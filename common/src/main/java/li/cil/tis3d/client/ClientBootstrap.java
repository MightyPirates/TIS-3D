package li.cil.tis3d.client;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import li.cil.tis3d.client.manual.Manuals;
import li.cil.tis3d.client.renderer.color.CasingBlockColor;
import li.cil.tis3d.client.renderer.entity.NullEntityRenderer;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.entity.Entities;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ClientBootstrap {
    public static void run() {
        ColorHandlerRegistry.registerBlockColors(new CasingBlockColor(), Blocks.CASING);
        EntityRendererRegistry.register(Entities.INFRARED_PACKET, NullEntityRenderer::new);
    }
}
