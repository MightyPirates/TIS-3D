package li.cil.tis3d.client.renderer.block.forge;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

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
    public BakedModel bake(final IGeometryBakingContext context, final ModelBaker baker, final Function<Material, TextureAtlasSprite> spriteGetter, final ModelState modelState, final ItemOverrides overrides, final ResourceLocation modelLocation) {
        return new ModuleBakedModel(proxy.bake(context, baker, spriteGetter, modelState, overrides, modelLocation));
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> modelGetter, final IGeometryBakingContext context) {
        proxy.resolveParents(modelGetter, context);
    }

    @Override
    public Set<String> getConfigurableComponentNames() {
        return proxy.getConfigurableComponentNames();
    }
}
