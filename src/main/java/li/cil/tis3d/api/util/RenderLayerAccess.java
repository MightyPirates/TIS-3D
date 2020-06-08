package li.cil.tis3d.api.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public abstract class RenderLayerAccess extends RenderLayer {
    /**
     * Not meant to be instantiated.
     */
    private RenderLayerAccess() {
        super("", VertexFormats.POSITION, 0, 0, false, false, null, null);
    }

    /**
     * Create a render layer that is identical to
     * {@link net.minecraft.client.render.RenderLayer#getEntityCutout},
     * except with diffuse lighting disabled.
     *
     * @param texture the id of the texture to be bound.
     * @return the {@link net.minecraft.client.render.RenderLayer} instance.
     */
    public static RenderLayer getCutoutNoDiffLight(final Identifier texture) {
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
