package li.cil.manual.api.provider;

import li.cil.manual.api.ContentRenderer;
import li.cil.manual.api.ManualFilter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

/**
 * Image providers are used to render custom content in a page. These are
 * selected via the standard image tag of Markdown, based on the prefix of
 * the image URL, i.e. <tt>![tooltip](prefix:data)</tt> will select the
 * image provider registered for the prefix <tt>prefix</tt>, and pass to
 * it the argument <tt>data</tt>, then use the returned renderer to draw
 * an element in the place of the tag.
 * <p>
 * Custom providers are only selected if a prefix is matched, otherwise
 * it'll treat it as a relative path to an image to load via Minecraft's
 * resource providing facilities, and display that.
 */
@OnlyIn(Dist.CLIENT)
public interface RendererProvider extends ManualFilter<RendererProvider> {
    /**
     * Tries to get an image renderer for the specified data. If the specified
     * path is not supported, {@link Optional#empty()} is returned.
     * <p>
     * Implementations should check if the path starts with the prefix this provider
     * is responsible for.
     * <p>
     * If there is no appropriate image renderer (for example, for the built-in
     * item stack renderers: if the item definition is invalid), this should
     * return an error graphic, it should <em>never</em> throw an exception.
     *
     * @param path the path to the requested image content.
     * @return the image renderer for the data, or {@link Optional#empty()} if
     * the specified path is not supported.
     */
    Optional<ContentRenderer> getRenderer(final String path);
}
