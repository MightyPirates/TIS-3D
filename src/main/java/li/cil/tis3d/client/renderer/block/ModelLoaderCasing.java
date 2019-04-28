package li.cil.tis3d.client.renderer.block;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.Constants;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public enum ModelLoaderCasing implements ICustomModelLoader {
    INSTANCE;

    // --------------------------------------------------------------------- //

    public static final ResourceLocation LOCATION_CASING = new ResourceLocation(API.MOD_ID, Constants.NAME_BLOCK_CASING);
    public static final ResourceLocation LOCATION_CASING_BASE = new ResourceLocation(API.MOD_ID, Constants.NAME_BLOCK_CASING + "_base");

    // --------------------------------------------------------------------- //
    // ICustomModelLoader

    @Override
    public boolean accepts(final ResourceLocation modelLocation) {
        // Accept for all variants: do equals using our actual ResourceLocation, not the passed ModelResourceLocation.
        return LOCATION_CASING.equals(modelLocation);
    }

    @Override
    public IModel loadModel(final ResourceLocation resourceLocation) throws Exception {
        if (resourceLocation instanceof ModelResourceLocation) {
            final ModelResourceLocation modelLocation = (ModelResourceLocation) resourceLocation;
            final ModelResourceLocation baseLocation = new ModelResourceLocation(LOCATION_CASING_BASE, modelLocation.getVariant());
            return new ModelCasing(ModelLoaderRegistry.getModel(baseLocation));
        }

        return ModelLoaderRegistry.getMissingModel();
    }

    @Override
    public void onResourceManagerReload(final IResourceManager resourceManager) {
    }
}
