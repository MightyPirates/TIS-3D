package li.cil.tis3d.client.manual.provider;

import li.cil.tis3d.api.manual.ContentRenderer;
import li.cil.tis3d.api.prefab.manual.AbstractRendererProvider;
import li.cil.tis3d.client.manual.Strings;
import li.cil.tis3d.client.manual.segment.render.MissingContentRenderer;
import li.cil.tis3d.client.manual.segment.render.TextureContentRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public final class TextureRendererProvider extends AbstractRendererProvider {
    public TextureRendererProvider() {
        super("texture");
    }

    @Override
    protected Optional<ContentRenderer> doGetRenderer(final String data) {
        try {
            return Optional.of(new TextureContentRenderer(new ResourceLocation(data)));
        } catch (final Throwable t) {
            return Optional.of(new MissingContentRenderer(Strings.WARNING_IMAGE_MISSING));
        }
    }
}
