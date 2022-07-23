package li.cil.tis3d.api.module.traits;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.OptionalInt;

/**
 * Modules implementing this interface will be queried when the quads for rendering the side of the
 * {@link li.cil.tis3d.api.machine.Casing} the module is currently installed in are collected.
 * <p>
 * Used for example by the facade module. Note that returning anything non-null will lead to <em>only</em> the
 * returned quads being used. Also note that this is called directly from
 * {@link net.minecraft.client.resources.model.BakedModel#getQuads(BlockState, Direction, RandomSource, ModelData, RenderType)},
 * so calls to this method may not come from the main thread.
 */
public interface ModuleWithBakedModel {
    /**
     * Whether this module has a module to render.
     * <p>
     * If this returns {@code false}, none of the other methods will be called, and the regular
     * blank module face will be rendered.
     * <p>
     * This supports dynamically changing models, which is used by the facade module, for example.
     *
     * @return whether this module has a model.
     */
    default boolean hasModel() {
        return true;
    }

    /**
     * Collect model data that is needed to render the quads returned by this module.
     * <p>
     * The returned value will be passed back into {@link #getQuads(BlockState, Direction, RandomSource, ModelData, RenderType)}.
     *
     * @param world the render-thread safe world access.
     * @param pos   the position of the casing.
     * @param state the current block-state of the casing.
     * @param data  the incoming model data to wrap/expand.
     * @return model data needed for rendering.
     */
    @OnlyIn(Dist.CLIENT)
    default ModelData getModelData(final BlockAndTintGetter world, final BlockPos pos, final BlockState state, final ModelData data) {
        return data;
    }

    /**
     * Called to obtain quads to use for the specified side instead of the casing's default ones. Will be called
     * directly from the casing's {@link net.minecraft.client.resources.model.BakedModel#getQuads(BlockState, Direction, RandomSource, ModelData, RenderType)}
     * logic.
     *
     * @param state      the casing's block state.
     * @param face       the side to obtain replacement quads for.
     * @param random     the random seed to use for the quad generation.
     * @param renderType the render type.
     * @return the list of replacement quads, or <c>null</c> to use the default casing quads.
     */
    @OnlyIn(Dist.CLIENT)
    List<BakedQuad> getQuads(final @Nullable BlockState state, @Nullable final Direction face, final RandomSource random, final ModelData data, @Nullable final RenderType renderType);

    /**
     * Returns the render types required by the underlying model.
     *
     * @param random the random seed to use for the quad generation.
     * @param data   the model data for the underlying model.
     * @return the render layers needed by the underlying model.
     */
    @OnlyIn(Dist.CLIENT)
    ChunkRenderTypeSet getRenderTypes(final RandomSource random, final ModelData data);

    /**
     * Get the tint color to use for the quads returned by this module.
     * <p>
     * Note that only the first tint color by any module in one casing will be used. Using facades with varying tint
     * color in a single casing will lead to wrong results. There's no way around it, so we just live with it.
     *
     * @param level     the render-thread safe world access.
     * @param pos       the position of the casing.
     * @param tintIndex the tint index to resolve.
     * @return the color for the specified tint index, if possible.
     */
    @OnlyIn(Dist.CLIENT)
    default OptionalInt getTintColor(@Nullable final BlockAndTintGetter level, @Nullable final BlockPos pos, final int tintIndex) {
        return OptionalInt.empty();
    }
}
