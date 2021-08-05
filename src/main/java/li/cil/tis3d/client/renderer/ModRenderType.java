package li.cil.tis3d.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import li.cil.tis3d.api.API;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public abstract class ModRenderType extends RenderType {
    private static final RenderType UNLIT_ATLAS_TEXTURE = create("atlas_module_overlay",
        DefaultVertexFormat.POSITION_COLOR_TEX,
        builder -> builder
            .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
            .setTextureState(BLOCK_SHEET_MIPPED));

    private static final RenderType UNLIT = create("module_overlay",
        DefaultVertexFormat.POSITION_COLOR,
        builder -> builder
            .setShaderState(RenderStateShard.POSITION_COLOR_SHADER));

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
            DefaultVertexFormat.POSITION_COLOR_TEX,
            builder -> builder
                .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false)));
    }

    // --------------------------------------------------------------------- //

    private static RenderType create(final String name, final VertexFormat format, final Function<CompositeState.CompositeStateBuilder, CompositeState.CompositeStateBuilder> parameters) {
        return create(API.MOD_ID + "/" + name,
            format,
            VertexFormat.Mode.QUADS, 256,
            false,
            false,
            parameters.apply(CompositeState.builder())
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(TRANSLUCENT_TARGET)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false));
    }

    // --------------------------------------------------------------------- //

    private ModRenderType() {
        super("", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 256, false, false, () -> {}, () -> {});
        throw new UnsupportedOperationException("Not meant to be instantiated.");
    }
}
