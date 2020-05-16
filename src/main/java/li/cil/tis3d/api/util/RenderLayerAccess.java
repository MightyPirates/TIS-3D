package li.cil.tis3d.api.util;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;


public abstract class RenderLayerAccess extends RenderLayer {
    private RenderLayerAccess() {
        // Not meant to be instantiated
        super("", VertexFormats.POSITION, 0, 0, false, false, null, null);
    }

    public static RenderLayer getCutoutNoDiffLight(Identifier texture) {
        final RenderLayer.MultiPhaseParameters parameters =
        RenderLayer.MultiPhaseParameters.builder()
        .texture(new RenderPhase.Texture(texture, false, false))
        .transparency(NO_TRANSPARENCY)
        .diffuseLighting(DISABLE_DIFFUSE_LIGHTING)
        .alpha(ONE_TENTH_ALPHA)
        .lightmap(ENABLE_LIGHTMAP)
        .overlay(ENABLE_OVERLAY_COLOR)
        .build(true);

        return RenderLayer.of("tis3d/cutout_no_difflight",
                              VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                              GL11.GL_QUADS, 256, false, false, parameters);
    }
}
