package li.cil.tis3d.client.renderer;

import li.cil.tis3d.api.API;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public abstract class RenderLayerAccess extends RenderType {
    private static final RenderType MODULE_OVERLAYS = create(API.MOD_ID + "/module_overlay",
        DefaultVertexFormats.POSITION_COLOR_TEX,
        GL11.GL_QUADS, 4 * 1024,
        false,
        false,
        State.builder()
            .setTextureState(BLOCK_SHEET_MIPPED)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDiffuseLightingState(NO_DIFFUSE_LIGHTING)
            .setAlphaState(DEFAULT_ALPHA)
            .setLightmapState(NO_LIGHTMAP)
            .setOutputState(TRANSLUCENT_TARGET)
            .setWriteMaskState(COLOR_WRITE)
            .createCompositeState(false));

    // --------------------------------------------------------------------- //

    /**
     * Create a render layer that is identical to {@link RenderType#entityCutout(ResourceLocation)},
     * except with diffuse lighting disabled.
     *
     * @param texture the id of the texture to be bound.
     * @return the {@link RenderType} instance.
     */
    public static RenderType getModuleOverlay(final ResourceLocation texture) {
        final State parameters = State.builder()
            .setTextureState(new TextureState(texture, false, false))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setDiffuseLightingState(NO_DIFFUSE_LIGHTING)
            .setAlphaState(DEFAULT_ALPHA)
            .setLightmapState(NO_LIGHTMAP)
            .setOutputState(TRANSLUCENT_TARGET)
            .setWriteMaskState(COLOR_WRITE)
            .createCompositeState(false);

        return create(API.MOD_ID + "/module_overlay",
            DefaultVertexFormats.POSITION_COLOR_TEX,
            GL11.GL_QUADS, 256, false, false, parameters);
    }

    /**
     * Render layer intended for modules, intended for rendering layered, transparent overlays.
     * As such, depth write is disabled in this layer. Textures must be in the block texture
     * atlas.
     *
     * @return the {@link RenderType} instance.
     */
    public static RenderType getModuleOverlay() {
        return MODULE_OVERLAYS;
    }

    // --------------------------------------------------------------------- //

    private RenderLayerAccess() {
        super("", DefaultVertexFormats.POSITION, 0, 256, false, false, () -> {}, () -> {});
        throw new UnsupportedOperationException("No meant to be instantiated.");
    }
}
