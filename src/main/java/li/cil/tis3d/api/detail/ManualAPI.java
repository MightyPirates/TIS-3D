package li.cil.tis3d.api.detail;

import li.cil.tis3d.api.manual.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface ManualAPI {
    /**
     * Register a tab to be displayed next to the manual.
     * <p>
     * These are intended to link to index pages, and for the time being there
     * a relatively low number of tabs that can be displayed, so I'd ask you to
     * only register as many tabs as actually, technically *needed*. Which will
     * usually be one, for your main index page.
     *
     * @param renderer the renderer used to render the icon on your tab.
     * @param tooltip  the unlocalized tooltip of the tab, or <tt>null</tt>.
     * @param path     the path to the page to open when the tab is clicked.
     */
    void addTab(final TabIconRenderer renderer, @Nullable final String tooltip, final String path);

    /**
     * Register a path provider.
     * <p>
     * Path providers are used to find documentation entries for item stacks
     * and blocks in the world.
     *
     * @param provider the provider to register.
     */
    void addProvider(final PathProvider provider);

    /**
     * Register a content provider.
     * <p>
     * Content providers are used to resolve paths to page content, if the
     * standard system (using Minecraft's resource loading facilities) fails.
     * <p>
     * This can be useful for providing dynamic content, for example.
     *
     * @param provider the provider to register.
     */
    void addProvider(final ContentProvider provider);

    /**
     * Register an image provider.
     * <p>
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
     *
     * @param prefix   the prefix on which to use the provider.
     * @param provider the provider to register.
     */
    void addProvider(final String prefix, final ImageProvider provider);

    // ----------------------------------------------------------------------- //

    /**
     * Look up the documentation path for the specified item stack.
     *
     * @param stack the stack to find the documentation path for.
     * @return the path to the page, <tt>null</tt> if none is known.
     */
    @Nullable
    String pathFor(final ItemStack stack);

    /**
     * Look up the documentation for the specified block in the world.
     *
     * @param world the world containing the block.
     * @param pos   the position of the block.
     * @return the path to the page, <tt>null</tt> if none is known.
     */
    @Nullable
    String pathFor(final World world, final BlockPos pos);

    /**
     * Get the content of the documentation page at the specified location.
     * <p>
     * The provided path may contain the special variable <tt>%LANGUAGE%</tt>,
     * which will be resolved to the currently set language, falling back to
     * <tt>en_US</tt>.
     *
     * @param path the path of the page to get the content of.
     * @return the content of the page, or <tt>null</tt> if none exists.
     */
    @Environment(EnvType.CLIENT)
    @Nullable
    Iterable<String> contentFor(final String path);

    /**
     * Get the image renderer for the specified image path.
     * <p>
     * This will look for {@link ImageProvider}s registered for a prefix in the
     * specified path. If there is no match, or the matched content provider
     * does not provide a renderer, this will return <tt>null</tt>.
     *
     * @param path the path to the image to get the renderer for.
     * @return the custom renderer for that path, or <tt>null</tt> if none exists.
     */
    @Environment(EnvType.CLIENT)
    @Nullable
    ImageRenderer imageFor(final String path);

    // ----------------------------------------------------------------------- //

    /**
     * Open the manual for the specified player.
     * <p>
     * If you wish to display a specific page, call {@link #navigate(String)}
     * after this function returns, with the path to the page to show.
     *
     * @param player the player to open the manual for.
     */
    @Environment(EnvType.CLIENT)
    void openFor(final PlayerEntity player);

    /**
     * Reset the history of the manual.
     */
    void reset();

    /**
     * Navigate to a page in the manual.
     *
     * @param path the path to navigate to.
     */
    @Environment(EnvType.CLIENT)
    void navigate(final String path);
}
