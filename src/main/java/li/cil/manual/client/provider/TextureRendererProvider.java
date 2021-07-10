package li.cil.manual.client.provider;

import li.cil.manual.api.render.ContentRenderer;
import li.cil.manual.api.ManualModel;
import li.cil.manual.api.prefab.provider.AbstractRendererProvider;
import li.cil.manual.client.document.Strings;
import li.cil.manual.client.document.segment.render.MissingContentRenderer;
import li.cil.manual.client.document.segment.render.TextureContentRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public final class TextureRendererProvider extends AbstractRendererProvider {
    public TextureRendererProvider() {
        super("texture");
    }

    // --------------------------------------------------------------------- //

    @Override
    public boolean matches(final ManualModel manual) {
        return true;
    }

    @Override
    protected Optional<ContentRenderer> doGetRenderer(final String data) {
        try {
            return Optional.of(new TextureContentRenderer(new ResourceLocation(data)));
        } catch (final Throwable t) {
            return Optional.of(new MissingContentRenderer(Strings.NO_SUCH_IMAGE));
        }
    }
}
