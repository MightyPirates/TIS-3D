package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.CommonAPI;
import li.cil.tis3d.api.manual.ImageProvider;
import li.cil.tis3d.api.manual.ImageRenderer;
import li.cil.tis3d.client.manual.segment.render.MissingItemRenderer;
import li.cil.tis3d.client.manual.segment.render.TextureImageRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class TextureImageProvider implements ImageProvider {
    private static final String WARNING_IMAGE_MISSING = CommonAPI.MOD_ID + ".manual.warning.missing.image";

    @Override
    public ImageRenderer getImage(final String data) {
        try {
            return new TextureImageRenderer(new Identifier(data));
        } catch (final Throwable t) {
            return new MissingItemRenderer(WARNING_IMAGE_MISSING);
        }
    }
}
