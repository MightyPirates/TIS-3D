/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.client.renderer.block.fabric;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.traits.fabric.ModuleWithBakedModelFabric;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Predicate;

public final class ModuleBakedModel implements BlockStateModel, FabricBlockStateModel {
    private final BlockStateModel proxy;
    private final Direction direction;

    // --------------------------------------------------------------------- //

    ModuleBakedModel(final BlockStateModel proxy, final Direction direction) {
        this.proxy = proxy;
        this.direction = direction;
    }

    // --------------------------------------------------------------------- //
    // FabricBlockStateModel

    @Override
    public void emitQuads(final QuadEmitter emitter, final BlockAndTintGetter blockView, final BlockPos pos,
                          final BlockState state, final RandomSource random, final Predicate<Direction> cullTest) {
        if (blockView.getBlockEntity(pos) instanceof final CasingBlockEntity casing) {
            final var module = casing.getModule(Face.fromDirection(direction));
            if (module instanceof final ModuleWithBakedModelFabric moduleWithModel && moduleWithModel.hasModel()) {
                moduleWithModel.emitBlockQuads(blockView, state, pos, direction, random, emitter);
                return;
            }
        }

        proxy.emitQuads(emitter, blockView, pos, state, random, cullTest);
    }

    // --------------------------------------------------------------------- //
    // BlockStateModel

    @Override
    public void collectParts(final RandomSource random, final List<BlockModelPart> parts) {
        proxy.collectParts(random, parts);
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return proxy.particleIcon();
    }
}
