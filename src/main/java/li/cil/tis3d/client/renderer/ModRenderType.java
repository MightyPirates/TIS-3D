package li.cil.tis3d.client.renderer;

import li.cil.tis3d.api.API;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public abstract class ModRenderType extends RenderType {
    private static final RenderType UNLIT_ATLAS_TEXTURE = create("atlas_module_overlay",
        DefaultVertexFormats.POSITION_COLOR_TEX,
        builder -> builder.setTextureState(BLOCK_SHEET_MIPPED));

    private static final RenderType UNLIT = create("module_overlay",
        DefaultVertexFormats.POSITION_COLOR,
        builder -> builder);

    // --------------------------------------------------------------------- //

    /**
     * Render layer intended for modules, intended for rendering layered, transparent overlays.
     * As such, depth write is disabled in this layer. Textures must be in the block texture
     * atlas.
     *
     * @return the {@link RenderType} instance.
     */
    public static RenderType unlit() {
        return UNLIT;
    }

    /**
     * Render layer intended for modules, intended for rendering layered, transparent overlays.
     * As such, depth write is disabled in this layer. Textures must be in the block texture
     * atlas.
     *
     * @return the {@link RenderType} instance.
     */
    public static RenderType unlitAtlasTexture() {
        return UNLIT_ATLAS_TEXTURE;
    }

    /**
     * Create a render layer that is identical to {@link RenderType#entityCutout(ResourceLocation)},
     * except with diffuse lighting disabled.
     *
     * @param texture the id of the texture to be bound.
     * @return the {@link RenderType} instance.
     */
    public static RenderType unlitTexture(final ResourceLocation texture) {
        return create("texture_module_overlay",
            DefaultVertexFormats.POSITION_COLOR_TEX,
            builder -> builder.setTextureState(new TextureState(texture, false, false)));
    }

    // --------------------------------------------------------------------- //

    private static RenderType create(final String name, final VertexFormat format, final Function<State.Builder, State.Builder> parameters) {
        return create(API.MOD_ID + "/" + name,
            format,
            GL11.GL_QUADS, 256,
            false,
            false,
            parameters.apply(State.builder())
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setDiffuseLightingState(NO_DIFFUSE_LIGHTING)
                .setAlphaState(DEFAULT_ALPHA)
                .setLightmapState(NO_LIGHTMAP)
                .setOutputState(TRANSLUCENT_TARGET)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false));
    }

    // --------------------------------------------------------------------- //

    private ModRenderType() {
        super("", DefaultVertexFormats.POSITION, 0, 256, false, false, () -> {}, () -> {});
        throw new UnsupportedOperationException("No meant to be instantiated.");
    }
}
