/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.client.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import li.cil.tis3d.api.API;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public final class ModRenderType {
    public static final RenderPipeline UNLIT_PIPELINE = RenderPipeline
        .builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(API.MOD_ID, "pipeline/module_overlay"))
        .withVertexShader("core/position_color")
        .withFragmentShader("core/position_color")
        .withBlend(BlendFunction.TRANSLUCENT)
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
        .withDepthWrite(false)
        .build();

    public static final RenderPipeline UNLIT_TEXTURED_PIPELINE = RenderPipeline
        .builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath(API.MOD_ID, "pipeline/module_overlay_textured"))
        .withVertexShader("core/position_tex_color")
        .withFragmentShader("core/position_tex_color")
        .withSampler("Sampler0")
        .withBlend(BlendFunction.TRANSLUCENT)
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
        .withDepthWrite(false)
        .build();

    private static final RenderType UNLIT = RenderType.create(API.MOD_ID + "/module_overlay",
        RenderSetup.builder(UNLIT_PIPELINE).createRenderSetup());

    private static final RenderType UNLIT_ATLAS_TEXTURE = RenderType.create(API.MOD_ID + "/atlas_module_overlay",
        RenderSetup.builder(UNLIT_TEXTURED_PIPELINE)
            .withTexture("Sampler0", Textures.LOCATION_BLOCK_ATLAS)
            .createRenderSetup());

    private static final Function<Identifier, RenderType> UNLIT_TEXTURE = Util.memoize(texture ->
        RenderType.create(API.MOD_ID + "/texture_module_overlay",
            RenderSetup.builder(UNLIT_TEXTURED_PIPELINE)
                .withTexture("Sampler0", texture)
                .createRenderSetup()));

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
     * Create a render layer that is identical to {@link net.minecraft.client.renderer.rendertype.RenderTypes#entityCutout(Identifier)},
     * except with diffuse lighting disabled.
     *
     * @param texture the id of the texture to be bound.
     * @return the {@link RenderType} instance.
     */
    public static RenderType unlitTexture(final Identifier texture) {
        return UNLIT_TEXTURE.apply(texture);
    }

    // --------------------------------------------------------------------- //

    private ModRenderType() {
        throw new UnsupportedOperationException("Not meant to be instantiated.");
    }
}
