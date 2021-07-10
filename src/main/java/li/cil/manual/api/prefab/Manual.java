package li.cil.manual.api.prefab;

import com.google.common.io.Files;
import li.cil.manual.api.ManualModel;
import li.cil.manual.api.Tab;
import li.cil.manual.api.render.ContentRenderer;
import li.cil.manual.api.util.ComparableRegistryEntry;
import li.cil.manual.api.util.Constants;
import net.minecraft.client.Minecraft;
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

/**
 * Simple implementation of the {@link ManualModel} interface which should cover most use-cases.
 */
@OnlyIn(Dist.CLIENT)
public class Manual extends ForgeRegistryEntry<ManualModel> implements ManualModel {
    /**
     * The magic first characters indicating a redirect in a document, with the target path following.
     */
    private static final String REDIRECT_PRAGMA = "#redirect ";

    // ----------------------------------------------------------------------- //

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

    public Manual() {
        reset();
    }

    // ----------------------------------------------------------------------- //

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> pathFor(final ItemStack stack) {
        return find(Constants.PATH_PROVIDERS, provider -> provider.pathFor(stack));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> pathFor(final World world, final BlockPos pos, final Direction face) {
        return find(Constants.PATH_PROVIDERS, provider -> provider.pathFor(world, pos, face));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Iterable<String>> contentFor(final String path) {
        final String language = Minecraft.getInstance().getLanguageManager().getSelected().getCode();
        final Optional<Iterable<String>> content = contentFor(path.replace(LANGUAGE_KEY, language), language, new LinkedHashSet<>());
        return content.isPresent() ? content : contentFor(path.replace(LANGUAGE_KEY, FALLBACK_LANGUAGE), FALLBACK_LANGUAGE, new LinkedHashSet<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ContentRenderer> imageFor(final String path) {
        return find(Constants.RENDERER_PROVIDERS, provider -> provider.getRenderer(path));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Tab> getTabs() {
        return StreamSupport.stream(RegistryManager.ACTIVE.getRegistry(Constants.TABS).spliterator(), false).
            filter(tab -> tab.matches(this)).
            collect(Collectors.toList());
    }

    // ----------------------------------------------------------------------- //

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        history.clear();
        history.add(createHistoryEntry(StringUtils.stripStart(getStartPage(), "/")));
        historyIndex = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public boolean pop() {
        if (historyIndex <= 0) {
            return false;
        }

        historyIndex--;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String peek() {
        return history.get(historyIndex).path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolve(final String path) {
        return resolve(peek(), path);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getUserData(final Class<T> type) {
        return Optional.ofNullable((T) history.get(historyIndex).userData.get(type));
    }

    /**
     * {@inheritDoc}
     */
    public <T> void setUserData(final T value) {
        history.get(historyIndex).userData.put(value.getClass(), value);
    }

    // ----------------------------------------------------------------------- //

    protected String getStartPage() {
        return LANGUAGE_KEY + "/index.md";
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
