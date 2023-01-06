package li.cil.tis3d.client.renderer.block.fabric;

import li.cil.tis3d.api.API;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

public final class ModuleModelLoader implements ModelResourceProvider {
    private static final ResourceLocation BLOCK_CASING_MODULE_MODEL_LOCATION = new ResourceLocation(API.MOD_ID, "block/casing_module");

    @Override
    public UnbakedModel loadModelResource(final ResourceLocation resourceId, final ModelProviderContext context) {
        if (resourceId.equals(BLOCK_CASING_MODULE_MODEL_LOCATION)) {
            return new ModuleUnbakedModel(context.loadModel(new ResourceLocation(resourceId.getNamespace(), resourceId.getPath() + "_proxy")));
        } else {
            return null;
        }
    }
}
