package li.cil.tis3d.api.manual;

import li.cil.tis3d.api.prefab.manual.NamespaceContentProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Optional;

/**
 * This interface allows implementation of content providers for the manual.
 * <p>
 * Content providers can be used to provide possibly dynamic page content for
 * arbitrary paths. Note that content providers have <em>lower</em> priority
 * than content found in resource packs, i.e. content providers will only be
 * queried for missing pages, so to speak.
 *
 * @see NamespaceContentProvider
 */
@OnlyIn(Dist.CLIENT)
public interface ContentProvider extends IForgeRegistryEntry<ContentProvider> {
    /**
     * Called to get the content of a path pointed to by the specified path.
     * <p>
     * This should provide an iterable over the lines of a Markdown document
     * (with the formatting provided by the in-game manual, which is a small
     * subset of "normal" Markdown).
     * <p>
     * If this provider cannot provide the requested path, it should return
     * <tt>null</tt> to indicate so, allowing other providers to be queried.
     *
     * @param path     the path to the manual page we're looking for.
     * @param language the language of the content to look up.
     * @return the content of the document at that path, or <tt>null</tt>.
     */
    Optional<Iterable<String>> getContent(final String path, final String language);
}
