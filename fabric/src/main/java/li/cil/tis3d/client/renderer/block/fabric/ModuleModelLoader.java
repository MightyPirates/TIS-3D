package li.cil.tis3d.client.renderer.block.fabric;

import li.cil.tis3d.api.API;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

public final class ModuleModelLoader implements ModelLoadingPlugin, ModelModifier.OnLoad {
    private static final ResourceLocation BLOCK_CASING_MODULE_MODEL_LOCATION = new ResourceLocation(API.MOD_ID, "block/casing_module");

    public static void initialize() {
        ModelLoadingPlugin.register(new ModuleModelLoader());
    }

    @Override
    public void onInitializeModelLoader(ModelLoadingPlugin.Context context) {
        context.modifyModelOnLoad().register(this);
    }

    @Override
    public UnbakedModel modifyModelOnLoad(UnbakedModel model, ModelModifier.OnLoad.Context context) {
        if (context.id().equals(BLOCK_CASING_MODULE_MODEL_LOCATION)) {
            return new ModuleUnbakedModel(context.getOrLoadModel(new ResourceLocation(context.id().getNamespace(), context.id().getPath() + "_proxy")));
        } else {
            return model;
        }
    }
}
