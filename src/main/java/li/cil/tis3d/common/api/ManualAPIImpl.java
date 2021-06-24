package li.cil.tis3d.common.api;

import com.google.common.io.Files;
import li.cil.tis3d.api.detail.ManualAPI;
import li.cil.tis3d.api.manual.*;
import li.cil.tis3d.client.gui.ManualScreen;
import li.cil.tis3d.client.manual.Manual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.Language;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Pattern;

public final class ManualAPIImpl implements ManualAPI {
    private static final Logger LOGGER = LogManager.getLogger();

    // --------------------------------------------------------------------- //

    // Language placeholder replacement.
    private static final String LANGUAGE_KEY = "%LANGUAGE%";
    private static final String FALLBACK_LANGUAGE = "en_US";
    private static final Pattern PATTERN_LANGUAGE_KEY = Pattern.compile(LANGUAGE_KEY);

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
    @Nullable
    public String pathFor(final ItemStack stack) {
        return pathFor(p -> p.pathFor(stack), MESSAGE_PATH_PROVIDER_ITEM_EXCEPTION);
    }

    @Override
    @Nullable
    public String pathFor(final World world, final BlockPos pos, final Direction side) {
        return pathFor(p -> p.pathFor(world, pos, side), MESSAGE_PATH_PROVIDER_BLOCK_EXCEPTION);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    @Nullable
    public Iterable<String> contentFor(final String path) {
        final String cleanPath = Files.simplifyPath(path);
        final Language currentLanguage = Minecraft.getInstance().getLanguageManager().getCurrentLanguage();
        final Optional<Iterable<String>> result = contentForWithRedirects(PATTERN_LANGUAGE_KEY.matcher(cleanPath).replaceAll(currentLanguage.getCode()));
        if (result.isPresent()) {
            return result.get();
        }
        return contentForWithRedirects(PATTERN_LANGUAGE_KEY.matcher(cleanPath).replaceAll(FALLBACK_LANGUAGE)).orElse(null);
    }

    @Override
    @Nullable
    public ImageRenderer imageFor(final String href) {
        final IForgeRegistry<ImageProvider> imageProviders = Manual.IMAGE_PROVIDER_REGISTRY.get();
        for (final ImageProvider provider : imageProviders) {
            if (provider.matches(href)) {
                try {
                    final ImageRenderer image = provider.getImage(href);
                    if (image != null) {
                        return image;
                    }
                } catch (final Throwable t) {
                    LOGGER.warn(MESSAGE_IMAGE_PROVIDER_EXCEPTION, t);
                }
            }
        }

        return null;
    }

    @Override
    public void open() {
        Minecraft.getInstance().displayGuiScreen(new ManualScreen());
    }

    @Override
    public void reset() {
        history.clear();
        history.push(new History(String.format("%s/index.md", LANGUAGE_KEY)));
    }

    @Override
    public void navigate(final String path) {
        final Screen screen = Minecraft.getInstance().currentScreen;
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

    @Nullable
    private String pathFor(final ProviderQuery query, final String warning) {
        for (final PathProvider provider : Manual.PATH_PROVIDER_REGISTRY.get()) {
            try {
                final String path = query.pathFor(provider);
                if (path != null) {
                    return path;
                }
            } catch (final Throwable t) {
                LOGGER.warn(warning, t);
            }
        }
        return null;
    }

    private Optional<Iterable<String>> contentForWithRedirects(final String path) {
        return contentForWithRedirects(path, new ArrayList<>());
    }

    private Optional<Iterable<String>> contentForWithRedirects(final String path, final List<String> seen) {
        if (seen.contains(path)) {
            final List<String> message = new ArrayList<>();
            message.add("Redirection loop: ");
            message.addAll(seen);
            message.add(path);
            return Optional.of(message);
        }
        final Optional<Iterable<String>> content = doContentLookup(path);

        if (content.isPresent()) {
            final Iterable<String> lines = content.get();
            final Iterator<String> iterator = lines.iterator();
            if (iterator.hasNext()) {
                final String line = iterator.next();
                if (line.toLowerCase(Locale.US).startsWith("#redirect ")) {
                    final List<String> newSeen = new ArrayList<>(seen);
                    newSeen.add(path);
                    return contentForWithRedirects(makeRelative(line.substring("#redirect ".length()), path), newSeen);
                }
            }
        }
        return content; // Empty.
    }

    private Optional<Iterable<String>> doContentLookup(final String path) {
        for (final ContentProvider provider : Manual.CONTENT_PROVIDER_REGISTRY.get()) {
            try {
                final Iterable<String> lines = provider.getContent(path);
                if (lines != null) {
                    return Optional.of(lines);
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

    @FunctionalInterface
    private interface ProviderQuery {
        @Nullable
        String pathFor(PathProvider provider);
    }
}
