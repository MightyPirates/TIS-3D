package li.cil.tis3d.client.renderer.block.forge;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public final class ModuleModel implements IUnbakedGeometry<ModuleModel> {
    private final IUnbakedGeometry<?> proxy;

    // --------------------------------------------------------------------- //

    ModuleModel(final IUnbakedGeometry<?> proxy) {
        this.proxy = proxy;
    }

    // --------------------------------------------------------------------- //
    // IModelGeometry

    @Override
    public BakedModel bake(final IGeometryBakingContext context, final ModelBakery bakery, final Function<Material, TextureAtlasSprite> spriteGetter, final ModelState modelState, final ItemOverrides overrides, final ResourceLocation modelLocation) {
        return new ModuleBakedModel(proxy.bake(context, bakery, spriteGetter, modelState, overrides, modelLocation));
    }

    @Override
    public Collection<Material> getMaterials(final IGeometryBakingContext context, final Function<ResourceLocation, UnbakedModel> modelGetter, final Set<Pair<String, String>> missingTextureErrors) {
        return proxy.getMaterials(context, modelGetter, missingTextureErrors);
    }
}
