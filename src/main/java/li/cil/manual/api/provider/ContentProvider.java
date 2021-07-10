package li.cil.manual.api.provider;

import li.cil.manual.api.prefab.provider.NamespaceContentProvider;
import li.cil.manual.api.util.ComparableRegistryEntry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * This interface allows implementation of content providers for the manual.
 * <p>
 * Content providers can be used to provide possibly dynamic page content for
 * arbitrary paths.
 *
 * @see NamespaceContentProvider
 */
@OnlyIn(Dist.CLIENT)
public interface ContentProvider extends ComparableRegistryEntry<ContentProvider> {
    /**
     * Called to get the content of a path pointed to by the specified path.
     * <p>
     * This should provide an iterable over the lines of a Markdown document
     * (with the formatting provided by the in-game manual, which is a small
     * subset of "normal" Markdown).
     * <p>
     * If this provider cannot provide the requested path, it should return
     * {@code null} to indicate so, allowing other providers to be queried.
     *
     * @param path     the path to the manual page we're looking for.
     * @param language the language of the content to look up.
     * @return the content of the document at that path, or {@code null}.
     */
    Optional<Stream<String>> getContent(final String path, final String language);
}
