package li.cil.tis3d.client.renderer.block;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public final class ModuleModel implements IModelGeometry<ModuleModel> {
    private final ModelLoaderRegistry.VanillaProxy proxy;

    // --------------------------------------------------------------------- //

    ModuleModel(final ModelLoaderRegistry.VanillaProxy proxy) {
        this.proxy = proxy;
    }

    // --------------------------------------------------------------------- //
    // IModelGeometry

    @Override
    public IBakedModel bake(final IModelConfiguration owner, final ModelBakery bakery, final Function<RenderMaterial, TextureAtlasSprite> spriteGetter, final IModelTransform modelTransform, final ItemOverrideList overrides, final ResourceLocation modelLocation) {
        return new ModuleBakedModel(proxy.bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation));
    }

    @Override
    public Collection<RenderMaterial> getTextures(final IModelConfiguration owner, final Function<ResourceLocation, IUnbakedModel> modelGetter, final Set<Pair<String, String>> missingTextureErrors) {
        return proxy.getTextures(owner, modelGetter, missingTextureErrors);
    }
}
