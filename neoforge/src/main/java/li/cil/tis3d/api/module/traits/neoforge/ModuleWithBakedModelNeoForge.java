/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.api.module.traits.neoforge;

import li.cil.tis3d.api.module.traits.ModuleWithBakedModel;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * NeoForge specific specialization of the {@link ModuleWithBakedModel} interface. Use this when using
 * NeoForge to contribute custom geometry for a module.
 */
public interface ModuleWithBakedModelNeoForge extends ModuleWithBakedModel {
    void collectParts(final BlockAndTintGetter level, final BlockPos pos, final BlockState state, final Direction direction, final RandomSource random, final List<BlockModelPart> parts);
}
