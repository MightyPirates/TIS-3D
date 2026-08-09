package li.cil.tis3d.mixin.fabric;

import li.cil.tis3d.api.module.traits.fabric.ModuleWithBakedModelFabric;
import li.cil.tis3d.common.module.FacadeModule;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(FacadeModule.class)
public abstract class MixinFacadeModule implements ModuleWithBakedModelFabric {
    @Shadow
    private BlockState facadeState;

    @Override
    public void emitBlockQuads(final BlockAndTintGetter blockView, final BlockState state, final BlockPos pos, final Direction direction, final RandomSource random, final QuadEmitter emitter) {
        final var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(facadeState);
        final List<BlockModelPart> parts = new ArrayList<>();
        model.collectParts(random, parts);
        for (final BlockModelPart part : parts) {
            for (final var quad : part.getQuads(direction)) {
                emitter.fromBakedQuad(quad);
                emitter.renderLayer(ChunkSectionLayer.CUTOUT);
                emitter.emit();
            }
        }
    }
}
