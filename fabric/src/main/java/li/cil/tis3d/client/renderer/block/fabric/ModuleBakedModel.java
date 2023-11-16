package li.cil.tis3d.client.renderer.block.fabric;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.traits.fabric.ModuleWithBakedModelFabric;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public final class ModuleBakedModel implements BakedModel, FabricBakedModel {
    private final BakedModel proxy;
    private final Direction direction;

    // --------------------------------------------------------------------- //

    ModuleBakedModel(final BakedModel proxy, final Direction direction) {
        this.proxy = proxy;
        this.direction = direction;
    }

    // --------------------------------------------------------------------- //
    // FabricBakedModel

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(final BlockAndTintGetter blockView, final BlockState state, final BlockPos pos, final Supplier<RandomSource> randomSupplier, final RenderContext context) {
        if (!(blockView.getBlockEntity(pos) instanceof final CasingBlockEntity casing)) {
            return;
        }

        final var module = casing.getModule(Face.fromDirection(direction));
        if (module instanceof final ModuleWithBakedModelFabric moduleWithModel && moduleWithModel.hasModel()) {
            moduleWithModel.emitBlockQuads(blockView, state, pos, direction, randomSupplier, context);
        } else {
            proxy.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        }
    }

    @Override
    public void emitItemQuads(final ItemStack stack, final Supplier<RandomSource> randomSupplier, final RenderContext context) {
    }

    // --------------------------------------------------------------------- //
    // BakedModel

    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState blockState, @Nullable final Direction direction, final RandomSource randomSource) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return proxy.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return proxy.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return proxy.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return proxy.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return proxy.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return proxy.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return proxy.getOverrides();
    }
}
