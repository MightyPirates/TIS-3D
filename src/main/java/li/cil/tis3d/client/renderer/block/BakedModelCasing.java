package li.cil.tis3d.client.renderer.block;

import li.cil.tis3d.api.module.traits.CasingFaceQuadOverride;
import li.cil.tis3d.common.block.property.PropertyCasingFaceQuadOverrides;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.List;

public final class BakedModelCasing implements IBakedModel {
    private final IBakedModel baseModel;

    // --------------------------------------------------------------------- //

    BakedModelCasing(final IBakedModel baseModel) {
        this.baseModel = baseModel;
    }

    // --------------------------------------------------------------------- //
    // IBakedModel

    @Override
    public List<BakedQuad> getQuads(@Nullable final IBlockState state, @Nullable final EnumFacing side, final long rand) {
        if (side != null && state instanceof IExtendedBlockState) {
            final IExtendedBlockState extendedBlockState = (IExtendedBlockState)state;
            final CasingFaceQuadOverride[] stateOverrides = extendedBlockState.getValue(PropertyCasingFaceQuadOverrides.INSTANCE);
            if (stateOverrides != null) {
                final CasingFaceQuadOverride stateOverride = stateOverrides[side.getIndex()];
                if (stateOverride != null) {
                    final List<BakedQuad> quads = stateOverride.getCasingFaceQuads(state, side, rand);
                    if (quads != null) {
                        return quads;
                    }
                }
            }
        }

        return baseModel.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return baseModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return baseModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return baseModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return baseModel.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return baseModel.getOverrides();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return baseModel.getItemCameraTransforms();
    }
}
