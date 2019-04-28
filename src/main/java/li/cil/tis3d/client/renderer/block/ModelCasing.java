package li.cil.tis3d.client.renderer.block;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.animation.IClip;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

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
    public IBakedModel bake(final IModelState state, final VertexFormat format, final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return new BakedModelCasing(baseModel.bake(state, format, bakedTextureGetter));
    }

    @Override
    public IModelState getDefaultState() {
        return baseModel.getDefaultState();
    }

    @Override
    public Optional<? extends IClip> getClip(final String name) {
        return baseModel.getClip(name);
    }

    @Override
    public IModel process(final ImmutableMap<String, String> customData) {
        return baseModel.process(customData);
    }

    @Override
    public IModel smoothLighting(final boolean value) {
        return baseModel.smoothLighting(value);
    }

    @Override
    public IModel gui3d(final boolean value) {
        return baseModel.gui3d(value);
    }

    @Override
    public IModel uvlock(final boolean value) {
        return baseModel.uvlock(value);
    }

    @Override
    public IModel retexture(final ImmutableMap<String, String> textures) {
        return baseModel.retexture(textures);
    }
}
