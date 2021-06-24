package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.manual.ImageProvider;
import li.cil.tis3d.api.manual.ImageRenderer;
import li.cil.tis3d.client.manual.Strings;
import li.cil.tis3d.client.manual.segment.render.MissingItemRenderer;
import li.cil.tis3d.client.manual.segment.render.TextureImageRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class TextureImageProvider extends ForgeRegistryEntry<ImageProvider> implements ImageProvider {
    private static final String PREFIX = "resource:";

    @Override
    public boolean matches(final String path) {
        return path.contains(PREFIX);
    }

    @Override
    public ImageRenderer getImage(final String path) {
        final String data = path.substring(PREFIX.length());
        try {
            return new TextureImageRenderer(new ResourceLocation(data));
        } catch (final Throwable t) {
            return new MissingItemRenderer(Strings.WARNING_IMAGE_MISSING);
        }
    }
}
