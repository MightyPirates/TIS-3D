package li.cil.tis3d.client.renderer.block;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public final class ModuleModelLoader implements IModelLoader<ModuleModel> {
    // --------------------------------------------------------------------- //
    // IModelLoader

    @Override
    public void onResourceManagerReload(final IResourceManager resourceManager) {
    }

    @Override
    public ModuleModel read(final JsonDeserializationContext context, final JsonObject modelContents) {
        return new ModuleModel(ModelLoaderRegistry.VanillaProxy.Loader.INSTANCE.read(context, modelContents));
    }
}
