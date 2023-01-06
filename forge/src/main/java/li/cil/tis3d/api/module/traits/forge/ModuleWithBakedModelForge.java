package li.cil.tis3d.api.module.traits.forge;

import li.cil.tis3d.api.module.traits.ModuleWithBakedModel;
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

/**
 * Forge specific specialization of the {@link ModuleWithBakedModel} interface. Use this when using Forge
 * to emit custom quads for a module.
 */
public interface ModuleWithBakedModelForge extends ModuleWithBakedModel {
    /**
     * Collect model data that is needed to render the quads returned by this module.
     * <p>
     * The returned value will be passed back into {@link #getQuads(BlockState, Direction, RandomSource, ModelData, RenderType)}.
     *
     * @param level the render-thread safe world access.
     * @param pos   the position of the casing.
     * @param state the current block-state of the casing.
     * @param data  the incoming model data to wrap/expand.
     * @return model data needed for rendering.
     */
    @OnlyIn(Dist.CLIENT)
    default ModelData getModelData(final BlockAndTintGetter level, final BlockPos pos, final BlockState state, final ModelData data) {
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
}
