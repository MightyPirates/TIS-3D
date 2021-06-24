package li.cil.tis3d.api.manual;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

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
public interface ImageProvider extends IForgeRegistryEntry<ImageProvider> {
    /**
     * Tests whether this image provider works for the specified path.
     * <p>
     * Specifically, this should at least check if the path starts with the
     * prefix this provider is responsible for.
     *
     * @param path the path to check for.
     */
    boolean matches(String path);

    /**
     * Gets an image renderer for the specified data.
     * <p>
     * The data passed here will be part of the image URL following the prefix
     * that the provider was registered with. So for example, if the provider
     * was registered for the prefix <tt>custom</tt>, and the image to be
     * rendered in the Markdown document was <tt>[blah](custom:the data]</tt>,
     * then the string passed where would be <tt>the data</tt>.
     * <p>
     * If there is no appropriate image renderer (for example, for the built-in
     * item stack renderers: if the item definition is invalid), this should
     * return <tt>null</tt>, it should <em>never</em> throw an exception.
     *
     * @param data the data part of the image definition.
     * @return the image renderer for the data, or <tt>null</tt> if none exists.
     */
    @Nullable
    ImageRenderer getImage(final String data);
}
