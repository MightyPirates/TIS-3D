package li.cil.tis3d.api.module.traits;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Modules implementing this interface will be queried when the quads for rendering the side of the
 * {@link li.cil.tis3d.api.machine.Casing} the module is currently installed in are collected.
 * <p>
 * Used for example by the facade module. Note that returning anything non-null will lead to <em>only</em> the
 * returned quads being used. Also note that this is called directly from
 * {@link net.minecraft.client.renderer.model.IBakedModel#getQuads(BlockState, Direction, Random, IModelData)},
 * so calls to this method may not come from the main thread.
 */
public interface ModuleWithBakedModel {
    /**
     * Called to obtain quads to use for the specified side instead of the casing's default ones. May return
     * <c>null</c> to not override the default quads. Will be called directly from the casing's
     * {@link net.minecraft.client.renderer.model.IBakedModel#getQuads(BlockState, Direction, Random, IModelData)}
     * logic.
     *
     * @param state  the casing's block state.
     * @param side   the side to obtain replacement quads for.
     * @param random the random seed to use for the quad generation.
     * @return the list of replacement quads, or <c>null</c> to use the default casing quads.
     */
    @OnlyIn(Dist.CLIENT)
    List<BakedQuad> getQuads(final @Nullable BlockState state, @Nullable final Direction side, final Random random);
}
