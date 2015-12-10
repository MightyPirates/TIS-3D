package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.manual.ImageProvider;
import li.cil.tis3d.api.manual.ImageRenderer;
import li.cil.tis3d.client.manual.segment.render.MissingItemRenderer;
import li.cil.tis3d.client.manual.segment.render.TextureImageRenderer;
import net.minecraft.util.ResourceLocation;

public final class TextureImageProvider implements ImageProvider {
    public static final String WARNING_IMAGE_MISSING = API.MOD_ID + ".manual.warning.missing.image";

    @Override
    public ImageRenderer getImage(final String data) {
        try {
            return new TextureImageRenderer(new ResourceLocation(data));
        } catch (final Throwable t) {
            return new MissingItemRenderer(WARNING_IMAGE_MISSING);
        }
    }
}
