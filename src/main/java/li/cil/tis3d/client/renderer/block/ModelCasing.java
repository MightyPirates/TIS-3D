package li.cil.tis3d.client.renderer.block;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

import java.util.Collection;

public final class ModelCasing implements IModel {
    private final IModel baseModel;

    // --------------------------------------------------------------------- //

    ModelCasing(final IModel baseModel) {
        this.baseModel = baseModel;
    }

    // --------------------------------------------------------------------- //
    // IModel

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return baseModel.getDependencies();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return baseModel.getTextures();
    }

    @Override
    public IBakedModel bake(final IModelState state, final VertexFormat format, final com.google.common.base.Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new BakedModelCasing(baseModel.bake(state, format, bakedTextureGetter));
    }

    @Override
    public IModelState getDefaultState() {
        return baseModel.getDefaultState();
    }
}
