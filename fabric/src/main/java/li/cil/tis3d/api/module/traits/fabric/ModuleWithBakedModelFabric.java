package li.cil.tis3d.api.module.traits.fabric;

import li.cil.tis3d.api.module.traits.ModuleWithBakedModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Fabric specific specialization of the {@link ModuleWithBakedModel} interface. Use this when using Fabric
 * to emit custom quads for a module.
 */
public interface ModuleWithBakedModelFabric extends ModuleWithBakedModel {
    @Environment(EnvType.CLIENT)
    void emitBlockQuads(final BlockAndTintGetter blockView, final BlockState state, final BlockPos pos, final Direction direction, final Supplier<RandomSource> randomSupplier, final RenderContext context);
}
