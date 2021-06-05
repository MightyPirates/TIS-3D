package li.cil.tis3d.api.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.opengl.GL11;
/*
@Environment(EnvType.CLIENT)
public abstract class RenderLayerAccess extends RenderLayer {
    private static final String CUTOUT_NO_DIFFUSE_LIGHT_LAYER_NAME = "tis3d/cutout_no_difflight";

    /**
     * Not meant to be instantiated.
     /
    private RenderLayerAccess() {
        super("", VertexFormats.POSITION, VertexFormat.DrawMode.LINES, 0, false, false, null, null);
    }

    /**
     * Create a render layer that is identical to
     * {@link RenderLayer#getEntityCutout},
     * except with diffuse lighting disabled.
     *
     * @param texture the id of the texture to be bound.
     * @return the {@link RenderLayer} instance.
     /
    public static RenderLayer getCutoutNoDiffLight(final Identifier Texture) {
        return RenderLayer.ge
        return Util.memoize((ignore) -> {
            final MultiPhaseParameters parameters = MultiPhaseParameters
                .builder()
                .texture(new Texture(Texture, false, false))
                .transparency(NO_TRANSPARENCY)
                .depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
                .cull(RenderPhase.DISABLE_CULLING)
                .lightmap(ENABLE_LIGHTMAP)
                .overlay(ENABLE_OVERLAY_COLOR)
                .build(true);

            return of(CUTOUT_NO_DIFFUSE_LIGHT_LAYER_NAME,
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                VertexFormat.DrawMode.QUADS, 256, false, false, parameters);

        }).apply();
    }
}
*/