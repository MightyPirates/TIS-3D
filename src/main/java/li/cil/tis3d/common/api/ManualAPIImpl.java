package li.cil.tis3d.common.api;

import com.google.common.io.Files;
import li.cil.tis3d.api.detail.ManualAPI;
import li.cil.tis3d.api.manual.*;
import li.cil.tis3d.client.gui.ManualScreen;
import li.cil.tis3d.client.manual.Manual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public final class ManualAPIImpl implements ManualAPI {
    private static final Logger LOGGER = LogManager.getLogger();

    // --------------------------------------------------------------------- //

    // Error messages.
    private static final String MESSAGE_CONTENT_LOOKUP_EXCEPTION = "A content provider threw an error when queried.";
    private static final String MESSAGE_IMAGE_PROVIDER_EXCEPTION = "An image provider threw an error when queried.";
    private static final String MESSAGE_PATH_PROVIDER_ITEM_EXCEPTION = "A path provider threw an error when queried with an item.";
    private static final String MESSAGE_PATH_PROVIDER_BLOCK_EXCEPTION = "A path provider threw an error when queried with a block.";

    // --------------------------------------------------------------------- //

    public static final ManualAPIImpl INSTANCE = new ManualAPIImpl();

    public static int getHistorySize() {
        return INSTANCE.history.size();
    }

    public static void pushPath(final String path) {
        INSTANCE.history.push(new ManualAPIImpl.History(path));
    }

    public static String peekPath() {
        return INSTANCE.history.peek().path;
    }

    public static int peekOffset() {
        return INSTANCE.history.peek().offset;
    }

    public static void setOffset(final int offset) {
        INSTANCE.history.peek().offset = offset;
    }

    public static void popPath() {
        INSTANCE.history.pop();
    }

    public static Iterable<Tab> getTabs() {
        return Manual.TAB_REGISTRY.get();
    }

    // --------------------------------------------------------------------- //

    /**
     * The history of pages the player navigated through (like browser history).
     */
    private final Stack<History> history = new Stack<>();

    // --------------------------------------------------------------------- //

    private ManualAPIImpl() {
        reset();
    }

    // --------------------------------------------------------------------- //

    @Override
    public Optional<String> pathFor(final ItemStack stack) {
        return pathFor(p -> p.pathFor(stack), MESSAGE_PATH_PROVIDER_ITEM_EXCEPTION);
    }

    @Override
    public Optional<String> pathFor(final World world, final BlockPos pos, final Direction face) {
        return pathFor(p -> p.pathFor(world, pos, face), MESSAGE_PATH_PROVIDER_BLOCK_EXCEPTION);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Optional<Iterable<String>> contentFor(final String path) {
        final String cleanPath = Files.simplifyPath(path);
        final String currentLanguage = Minecraft.getInstance().getLanguageManager().getSelected().getCode();
        final Optional<Iterable<String>> result = contentForWithRedirects(cleanPath.replace(LANGUAGE_KEY, currentLanguage), currentLanguage, new LinkedHashSet<>());
        return result.isPresent() ? result : contentForWithRedirects(cleanPath.replace(LANGUAGE_KEY, FALLBACK_LANGUAGE), FALLBACK_LANGUAGE, new LinkedHashSet<>());
    }

    @Override
    public Optional<ContentRenderer> imageFor(final String href) {
        final IForgeRegistry<RendererProvider> imageProviders = Manual.IMAGE_PROVIDER_REGISTRY.get();
        for (final RendererProvider provider : imageProviders) {
            final Optional<ContentRenderer> renderer = provider.getRenderer(href);
            if (renderer.isPresent()) {
                return renderer;
            }
        }

        return Optional.empty();
    }

    @Override
    public void open() {
        Minecraft.getInstance().setScreen(new ManualScreen());
    }

    @Override
    public void reset() {
        history.clear();
        history.push(new History(LANGUAGE_KEY + "/index.md"));
    }

    @Override
    public void navigate(final String path) {
        final Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof ManualScreen) {
            ((ManualScreen) screen).pushPage(path);
        } else {
            history.push(new History(path));
        }
    }

    // --------------------------------------------------------------------- //

    /**
     * Makes the specified path relative to the specified base path.
     *
     * @param path the path to make relative.
     * @param base the path to make it relative to.
     * @return the relative path.
     */
    public static String makeRelative(final String path, final String base) {
        if (path.startsWith("/")) {
            return path;
        } else {
            final int lastSlash = base.lastIndexOf('/');
            if (lastSlash >= 0) {
                return base.substring(0, lastSlash + 1) + path;
            } else {
                return path;
            }
        }
    }

    // --------------------------------------------------------------------- //

    private Optional<String> pathFor(final Function<PathProvider, Optional<String>> query, final String warning) {
        for (final PathProvider provider : Manual.PATH_PROVIDER_REGISTRY.get()) {
            try {
                final Optional<String> path = query.apply(provider);
                if (path.isPresent()) {
                    return path;
                }
            } catch (final Throwable t) {
                LOGGER.warn(warning, t);
            }
        }
        return Optional.empty();
    }

    private Optional<Iterable<String>> contentForWithRedirects(final String path, final String language, final Set<String> seen) {
        if (!seen.add(path)) {
            final List<String> message = new ArrayList<>();
            message.add("Redirection loop: ");
            message.addAll(seen);
            message.add(path);
            return Optional.of(message);
        }

        try {
            final Optional<Iterable<String>> content = doContentLookup(path, language);

            if (content.isPresent()) {
                final Iterable<String> lines = content.get();
                final Iterator<String> iterator = lines.iterator();
                if (iterator.hasNext()) {
                    final String line = iterator.next();
                    if (line.toLowerCase(Locale.US).startsWith("#redirect ")) {
                        return contentForWithRedirects(makeRelative(line.substring("#redirect ".length()), path), language, seen);
                    }
                }
            }
            return content; // Empty.
        } finally {
            seen.remove(path);
        }
    }

    private Optional<Iterable<String>> doContentLookup(final String path, final String language) {
        for (final ContentProvider provider : Manual.CONTENT_PROVIDER_REGISTRY.get()) {
            try {
                final Optional<Iterable<String>> lines = provider.getContent(path, language);
                if (lines.isPresent()) {
                    return lines;
                }
            } catch (final Throwable t) {
                LOGGER.warn(MESSAGE_CONTENT_LOOKUP_EXCEPTION, t);
            }
        }
        return Optional.empty();
    }

    // --------------------------------------------------------------------- //

    public static final class History {
        public final String path;
        public int offset = 0;

        private History(final String path) {
            this.path = path;
        }
    }
}
