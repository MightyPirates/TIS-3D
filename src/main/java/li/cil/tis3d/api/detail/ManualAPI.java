package li.cil.tis3d.api.detail;

import li.cil.tis3d.api.manual.ContentRenderer;
import li.cil.tis3d.api.manual.RendererProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public interface ManualAPI {
    /**
     * The default language key in paths that will replaced by the actual language content will
     * be looked up for, typically the currently used language in the game.
     */
    String LANGUAGE_KEY = "%LANGUAGE%";

    /**
     * The language content look-up falls back to when no content is found for the currently active language.
     */
    String FALLBACK_LANGUAGE = "en_us";

    /**
     * Look up the documentation path for the specified item stack.
     *
     * @param stack the stack to find the documentation path for.
     * @return the path to the page, <tt>null</tt> if none is known.
     */
    Optional<String> pathFor(final ItemStack stack);

    /**
     * Look up the documentation for the specified block in the world.
     *
     * @param world the world containing the block.
     * @param pos   the position of the block.
     * @param face  the face of the block.
     * @return the path to the page, <tt>null</tt> if none is known.
     */
    Optional<String> pathFor(final World world, final BlockPos pos, final Direction face);

    /**
     * Get the content of the documentation page at the specified location.
     * <p>
     * The provided path may contain the special variable {@link ManualAPI#LANGUAGE_KEY},
     * which will be resolved to the currently set language, falling back to
     * {@link ManualAPI#FALLBACK_LANGUAGE}.
     *
     * @param path the path of the page to get the content of.
     * @return the content of the page, or <tt>null</tt> if none exists.
     */
    Optional<Iterable<String>> contentFor(final String path);

    /**
     * Get the image renderer for the specified image path.
     * <p>
     * This will look for {@link RendererProvider}s registered for a prefix in the
     * specified path. If there is no match, or the matched content provider
     * does not provide a renderer, this will return <tt>null</tt>.
     *
     * @param path the path to the image to get the renderer for.
     * @return the custom renderer for that path, or <tt>null</tt> if none exists.
     */
    Optional<ContentRenderer> imageFor(final String path);

    // ----------------------------------------------------------------------- //

    /**
     * Open the manual for the specified player.
     * <p>
     * If you wish to display a specific page, call {@link #navigate(String)}
     * after this function returns, with the path to the page to show.
     */
    void open();

    /**
     * Reset the history of the manual.
     */
    void reset();

    /**
     * Navigate to a page in the manual.
     *
     * @param path the path to navigate to.
     */
    void navigate(final String path);
}
