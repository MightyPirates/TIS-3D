package li.cil.tis3d.mixin.fabric;

import li.cil.tis3d.api.module.traits.fabric.ModuleWithBakedModelFabric;
import li.cil.tis3d.common.module.FacadeModule;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.IndigoRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;

@Mixin(FacadeModule.class)
public abstract class MixinFacadeModule implements ModuleWithBakedModelFabric {
    @Shadow
    private BlockState facadeState;

    @Override
    public void emitBlockQuads(final BlockAndTintGetter blockView, final BlockState state, final BlockPos pos, final Direction direction, final Supplier<RandomSource> randomSupplier, final RenderContext context) {
        final var emitter = context.getEmitter();
        final var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(facadeState);
        final var quads = model.getQuads(facadeState, direction, randomSupplier.get());
        for (final BakedQuad quad : quads) {
            emitter.fromVanilla(quad, IndigoRenderer.INSTANCE.materialFinder().blendMode(0, BlendMode.CUTOUT_MIPPED).find(), direction);
            emitter.emit();
        }
    }
}
