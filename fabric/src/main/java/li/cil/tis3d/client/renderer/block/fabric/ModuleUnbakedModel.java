package li.cil.tis3d.client.renderer.block.fabric;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

public final class ModuleUnbakedModel implements UnbakedModel {
    private final UnbakedModel proxy;

    // --------------------------------------------------------------------- //

    ModuleUnbakedModel(final UnbakedModel proxy) {
        this.proxy = proxy;
    }

    // --------------------------------------------------------------------- //
    // UnbakedModel

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return proxy.getDependencies();
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> function) {
        proxy.resolveParents(function);
    }

    @Nullable
    @Override
    public BakedModel bake(final ModelBaker modelBaker, final Function<Material, TextureAtlasSprite> function, final ModelState modelState, final ResourceLocation resourceLocation) {
        final var bakedProxy = this.proxy.bake(modelBaker, function, modelState, resourceLocation);
        if (bakedProxy != null) {
            return new ModuleBakedModel(bakedProxy, Direction.rotate(modelState.getRotation().getMatrix(), Direction.SOUTH));
        } else {
            return null;
        }
    }
}
