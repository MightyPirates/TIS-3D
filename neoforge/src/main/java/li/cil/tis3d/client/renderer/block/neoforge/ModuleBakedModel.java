package li.cil.tis3d.client.renderer.block.neoforge;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.traits.neoforge.ModuleWithBakedModelNeoForge;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;

import java.util.List;

public final class ModuleBakedModel implements DynamicBlockStateModel {
    private final BlockStateModel proxy;
    private final Direction direction;

    // --------------------------------------------------------------------- //

    ModuleBakedModel(final BlockStateModel proxy, final Direction direction) {
        this.proxy = proxy;
        this.direction = direction;
    }

    // --------------------------------------------------------------------- //
    // DynamicBlockStateModel

    @Override
    public void collectParts(final BlockAndTintGetter level, final BlockPos pos, final BlockState state,
                             final RandomSource random, final List<BlockModelPart> parts) {
        if (level.getBlockEntity(pos) instanceof final CasingBlockEntity casing) {
            final var module = casing.getModule(Face.fromDirection(direction));
            if (module instanceof final ModuleWithBakedModelNeoForge moduleWithModel && moduleWithModel.hasModel()) {
                moduleWithModel.collectParts(level, pos, state, direction, random, parts);
                return;
            }
        }

        proxy.collectParts(random, parts);
    }

    @Override
    public void collectParts(final RandomSource random, final List<BlockModelPart> parts) {
        proxy.collectParts(random, parts);
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return proxy.particleIcon();
    }
}
