package li.cil.manual.api;

import li.cil.manual.api.prefab.Manual;
import li.cil.manual.api.prefab.item.AbstractManualItem;
import li.cil.manual.api.provider.RendererProvider;
import li.cil.manual.api.render.ContentRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Optional;

/**
 * Represents the contents and navigation state of a single logical manual.
 * <p>
 * A model defines the set of providers specifying the contents of the manual and the current
 * navigation state, as well as means for resolving paths inside the manual's content.
 * <p>
 * Usually there will be an item representing a single logical manual. In the most simple case
 * this will be a class derived from {@link AbstractManualItem}.
 * <p>
 * Rendering of the manual, in the simple case, may be done using the built-in manual screen.
 * It may be opened using the {@link li.cil.manual.api.util.ShowManualScreenEvent}, which is,
 * by default, used by the {@link AbstractManualItem}. It may be customized by passing an
 * implementation of {@link ManualScreenStyle}.
 * <p>
 * Models must be registered with the manual model registry, so that providers may check if
 * they should provide data for them. This is particularly intended so that other mods may
 * contribute additional providers to any defined manual.
 *
 * @see AbstractManualItem
 * @see ManualStyle
 * @see ManualScreenStyle
 */
public interface ManualModel extends IForgeRegistryEntry<ManualModel> {
    /**
     * The default language key in paths that will replaced by the actual language content will
     * be looked up for, typically the currently used language in the game.
     */
    String LANGUAGE_KEY = "%LANGUAGE%";

    /**
     * The language content look-up falls back to when no content is found for the currently active language.
     */
    String FALLBACK_LANGUAGE = "en_us";

    // ----------------------------------------------------------------------- //

    /**
     * Look up the documentation path for the specified item stack.
     *
     * @param stack the stack to find the documentation path for.
     * @return the path to the page, {@code null} if none is known.
     */
    Optional<String> pathFor(ItemStack stack);

    /**
     * Look up the documentation for the specified block in the world.
     *
     * @param world the world containing the block.
     * @param pos   the position of the block.
     * @param face  the face of the block.
     * @return the path to the page, {@code null} if none is known.
     */
    Optional<String> pathFor(World world, BlockPos pos, Direction face);

    /**
     * Get the content of the documentation page at the specified location.
     * <p>
     * The provided path may contain the special variable {@link Manual#LANGUAGE_KEY},
     * which will be resolved to the currently set language, falling back to
     * {@link Manual#FALLBACK_LANGUAGE}.
     *
     * @param path the path of the page to get the content of.
     * @return the content of the page, or {@code null} if none exists.
     */
    Optional<Iterable<String>> contentFor(String path);

    /**
     * Get the image renderer for the specified image path.
     * <p>
     * This will look for {@link RendererProvider}s registered for a prefix in the
     * specified path. If there is no match, or the matched content provider
     * does not provide a renderer, this will return {@code null}.
     *
     * @param path the path to the image to get the renderer for.
     * @return the custom renderer for that path, or {@code null} if none exists.
     */
    Optional<ContentRenderer> imageFor(String path);

    /**
     * Gets the list of tabs that should be shown for this manual.
     *
     * @return the list of tabs.
     */
    Iterable<Tab> getTabs();

    // ----------------------------------------------------------------------- //

    /**
     * Clears the navigation history of the manual and pushes the default start page to the page stack.
     */
    void reset();

    /**
     * Pushes the page at the specified path in the manual onto the navigation history.
     * <p>
     * This may fail and result in no operation if the page on top the navigation history is the
     * page at the specified path.
     * <p>
     * Note that paths must not start with a leading slash ({@code /}).
     *
     * @param path the path to navigate to.
     * @throws IllegalArgumentException if {code path} starts with a slash.
     */
    void push(String path);

    /**
     * Tries to pop the top page from the navigation history.
     * <p>
     * This will never pop the last remaining page in the navigation history.
     *
     * @return {@code true} if a page was popped; {@code false} otherwise.
     */
    boolean pop();

    /**
     * Returns the path of the page currently on top of the page navigation stack.
     *
     * @return the path of the current page.
     */
    String peek();

    /**
     * Resolves a path relative to the current page.
     * <p>
     * If the specified path is an absolute path, returns the path unchanged. Otherwise
     * returns the absolute path to the page at the specified relative path based on the
     * current page's path.
     * <p>
     * Example:
     * <pre>
     * // peek() -> "home/index.md"
     * resolve("rel/path.md") // -> "home/re/path.md"
     * resolve("/abs/path.md") // -> "abs/path.md"
     * </pre>
     *
     * @param path the path to resolve.
     * @return the resolved path.
     */
    String resolve(String path);

    // ----------------------------------------------------------------------- //

    /**
     * Allows obtaining some typed userdata associated with the current manual page.
     *
     * @param type the type of the value to get.
     * @param <T>  the generic type of the value to get.
     * @return a value of the specified type, associated with the current page, if any.
     */
    <T> Optional<T> getUserData(final Class<T> type);

    /**
     * Associates the specified user data with the current manual page.
     *
     * @param value the value to associate with the current page.
     * @param <T>   the generic type of the value.
     */
    <T> void setUserData(final T value);
}
