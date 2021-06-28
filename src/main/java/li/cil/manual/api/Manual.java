package li.cil.manual.api;

import com.google.common.io.Files;
import li.cil.manual.api.prefab.ManualScreen;
import li.cil.manual.api.provider.RendererProvider;
import li.cil.manual.api.render.ContentRenderer;
import li.cil.manual.api.util.ComparableRegistryEntry;
import li.cil.manual.api.util.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@OnlyIn(Dist.CLIENT)
public class Manual extends ForgeRegistryEntry<Manual> {
    /**
     * The default language key in paths that will replaced by the actual language content will
     * be looked up for, typically the currently used language in the game.
     */
    public static final String LANGUAGE_KEY = "%LANGUAGE%";

    /**
     * The language content look-up falls back to when no content is found for the currently active language.
     */
    public static final String FALLBACK_LANGUAGE = "en_us";

    /**
     * The magic first characters indicating a redirect in a document, with the target path following.
     */
    private static final String REDIRECT_PRAGMA = "#redirect ";

    // ----------------------------------------------------------------------- //

    /**
     * The style to use when rendering the manual.
     */
    protected final Style style;

    /**
     * The current navigation history for this manual.
     */
    protected final List<History> history = new ArrayList<>();

    /**
     * The current index in the navigation history. We keep pages we came back from
     * at the back of the list to allow navigating back forwards, retaining the stored
     * scroll offset of these pages.
     */
    protected int historyIndex;

    // ----------------------------------------------------------------------- //

    public Manual(final Style style) {
        this.style = style;
        reset();
    }

    // ----------------------------------------------------------------------- //

    /**
     * Look up the documentation path for the specified item stack.
     *
     * @param stack the stack to find the documentation path for.
     * @return the path to the page, {@code null} if none is known.
     */
    public Optional<String> pathFor(final ItemStack stack) {
        return find(Constants.PATH_PROVIDERS, provider -> provider.pathFor(stack));
    }

    /**
     * Look up the documentation for the specified block in the world.
     *
     * @param world the world containing the block.
     * @param pos   the position of the block.
     * @param face  the face of the block.
     * @return the path to the page, {@code null} if none is known.
     */
    public Optional<String> pathFor(final World world, final BlockPos pos, final Direction face) {
        return find(Constants.PATH_PROVIDERS, provider -> provider.pathFor(world, pos, face));
    }

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
    public Optional<Iterable<String>> contentFor(final String path) {
        final String language = Minecraft.getInstance().getLanguageManager().getSelected().getCode();
        final Optional<Iterable<String>> content = contentFor(path.replace(LANGUAGE_KEY, language), language, new LinkedHashSet<>());
        return content.isPresent() ? content : contentFor(path.replace(LANGUAGE_KEY, FALLBACK_LANGUAGE), FALLBACK_LANGUAGE, new LinkedHashSet<>());
    }

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
    public Optional<ContentRenderer> imageFor(final String path) {
        return find(Constants.RENDERER_PROVIDERS, provider -> provider.getRenderer(path));
    }

    /**
     * Gets the list of tabs that should be shown for this manual.
     *
     * @return the list of tabs.
     */
    public Iterable<Tab> getTabs() {
        return StreamSupport.stream(RegistryManager.ACTIVE.getRegistry(Constants.TABS).spliterator(), false).
            filter(tab -> tab.matches(this)).
            collect(Collectors.toList());
    }

    // ----------------------------------------------------------------------- //

    /**
     * Open the manual for the specified player.
     * <p>
     * If you wish to display a specific page, call {@link #push(String)} with the path to the page to show.
     */
    public void open() {
        Minecraft.getInstance().setScreen(createScreen());
    }

    /**
     * Clears the navigation history of the manual and pushes the default start page to the page stack.
     */
    public void reset() {
        history.clear();
        history.add(createHistoryEntry(StringUtils.stripStart(getStartPage(), "/")));
        historyIndex = 0;
    }

    /**
     * Pushes the page at the specified path in the manual onto the navigation history, causing it
     * to be shown if the screen is visible or will be made visible by calling {@link #open()}.
     * <p>
     * This may fail and result in no operation if the page on top the navigation history is the
     * page at the specified path.
     * <p>
     * Note that paths must not start with a leading slash ({@code /}).
     *
     * @param path the path to navigate to.
     * @throws IllegalArgumentException if {code path} starts with a slash.
     */
    public void push(final String path) {
        if (path.startsWith("/")) throw new IllegalArgumentException("Path must not start with a slash.");
        if (Objects.equals(history.get(historyIndex).path, path)) {
            return;
        }

        // Try to re-use "future" entry.
        if (history.size() > historyIndex + 1 && Objects.equals(history.get(historyIndex + 1).path, path)) {
            historyIndex++;
            return;
        }

        // Remove "future" entries we kept to navigate back forwards.
        while (history.size() > historyIndex + 1) {
            history.remove(history.size() - 1);
        }

        history.add(createHistoryEntry(path));
        historyIndex++;
    }

    /**
     * Tries to pop the top page from the navigation history.
     * <p>
     * This will never pop the last remaining page in the navigation history.
     */
    public void pop() {
        if (historyIndex > 0) {
            historyIndex--;
            return;
        }

        final Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof ManualScreen && ((ManualScreen) screen).getManual() == this) {
            screen.onClose();
        }
    }

    /**
     * Returns the path of the page currently on top of the page navigation stack.
     *
     * @return the path of the current page.
     */
    public String peek() {
        return history.get(historyIndex).path;
    }

    /**
     * Allows obtaining some typed userdata associated with the current manual page.
     *
     * @param type the type of the value to get.
     * @param <T>  the generic type of the value to get.
     * @return a value of the specified type, associated with the current page, if any.
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getUserData(final Class<T> type) {
        return Optional.ofNullable((T) history.get(historyIndex).userData.get(type));
    }

    /**
     * Associates the specified user data with the current manual page.
     *
     * @param value the value to associate with the current page.
     * @param <T>   the generic type of the value.
     */
    public <T> void setUserData(final T value) {
        history.get(historyIndex).userData.put(value.getClass(), value);
    }

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
    public String resolve(final String path) {
        return resolve(peek(), path);
    }

    // ----------------------------------------------------------------------- //

    protected String getStartPage() {
        return LANGUAGE_KEY + "/index.md";
    }

    protected ManualScreen createScreen() {
        return new ManualScreen(this, style);
    }

    protected History createHistoryEntry(final String path) {
        return new History(path);
    }

    protected <TProvider extends ComparableRegistryEntry<TProvider>, TResult> Optional<TResult> find(final RegistryKey<Registry<TProvider>> key, final Function<TProvider, Optional<TResult>> lookup) {
        return RegistryManager.ACTIVE.getRegistry(key).getValues().stream().
            filter(provider -> provider.matches(this)).
            sorted().map(lookup).filter(Optional::isPresent).findFirst().flatMap(x -> x);
    }

    @SuppressWarnings("UnstableApiUsage")
    protected String resolve(final String base, final String path) {
        final String absolutePath;
        if (path.startsWith("/")) {
            absolutePath = StringUtils.stripStart(path, "/");
        } else {
            final int lastSlash = base.lastIndexOf('/');
            if (lastSlash >= 0) {
                absolutePath = base.substring(0, lastSlash + 1) + path;
            } else {
                absolutePath = path;
            }
        }

        return Files.simplifyPath(absolutePath);
    }

    protected Optional<Iterable<String>> contentFor(final String path, final String language, final Set<String> seen) {
        if (!seen.add(path)) {
            final List<String> message = new ArrayList<>();
            message.add("Redirection loop: ");
            message.addAll(seen);
            message.add(path);
            return Optional.of(message);
        }

        final Optional<List<String>> content = find(Constants.CONTENT_PROVIDERS, provider -> provider.getContent(path, language)).
            map(lines -> {
                final List<String> list = lines.collect(Collectors.toList());
                while (!list.isEmpty() && StringUtils.isWhitespace(list.get(0))) {
                    list.remove(0);
                }
                while (!list.isEmpty() && StringUtils.isWhitespace(list.get(list.size() - 1))) {
                    list.remove(list.size() - 1);
                }
                return list;
            });
        if (!content.isPresent()) {
            return Optional.empty(); // Page not found.
        }

        // Read first line only.
        final List<String> lines = content.get();
        if (lines.size() > 0 && lines.get(0).toLowerCase().startsWith(REDIRECT_PRAGMA)) {
            final String redirectPath = lines.get(0).substring(REDIRECT_PRAGMA.length()).trim();
            return contentFor(resolve(path, redirectPath), language, seen);
        }

        return Optional.of(lines); // Regular page.
    }

    // --------------------------------------------------------------------- //

    protected static class History {
        public final String path;
        public final Map<Class<?>, Object> userData = new HashMap<>();

        public History(final String path) {
            this.path = path;
        }
    }
}
