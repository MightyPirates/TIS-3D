package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.manual.RendererProvider;
import li.cil.tis3d.api.manual.ContentRenderer;
import li.cil.tis3d.client.manual.Strings;
import li.cil.tis3d.client.manual.segment.render.MissingItemRenderer;
import li.cil.tis3d.client.manual.segment.render.TextureContentRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class TextureRendererProvider extends ForgeRegistryEntry<RendererProvider> implements RendererProvider {
    private static final String PREFIX = "resource:";

    @Override
    public boolean matches(final String path) {
        return path.contains(PREFIX);
    }

    @Override
    public ContentRenderer getRenderer(final String path) {
        final String data = path.substring(PREFIX.length());
        try {
            return new TextureContentRenderer(new ResourceLocation(data));
        } catch (final Throwable t) {
            return new MissingItemRenderer(Strings.WARNING_IMAGE_MISSING);
        }
    }
}
