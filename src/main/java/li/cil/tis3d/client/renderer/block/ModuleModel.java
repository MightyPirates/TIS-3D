package li.cil.tis3d.client.renderer.block;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
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
    public BakedModel bake(final IModelConfiguration owner, final ModelBakery bakery, final Function<Material, TextureAtlasSprite> spriteGetter, final ModelState modelTransform, final ItemOverrides overrides, final ResourceLocation modelLocation) {
        return new ModuleBakedModel(proxy.bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation));
    }

    @Override
    public Collection<Material> getTextures(final IModelConfiguration owner, final Function<ResourceLocation, UnbakedModel> modelGetter, final Set<Pair<String, String>> missingTextureErrors) {
        return proxy.getTextures(owner, modelGetter, missingTextureErrors);
    }
}
