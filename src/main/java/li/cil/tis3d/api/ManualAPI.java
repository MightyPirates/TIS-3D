package li.cil.tis3d.api;

import li.cil.tis3d.api.manual.RendererProvider;
import li.cil.tis3d.api.manual.ContentRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * This API allows interfacing with the in-game manual of OpenComputers.
 * <p>
 * It allows opening the manual at a desired specific page, as well as
 * registering custom tabs and content callback handlers.
 * <p>
 * Note: this is a <em>client side only</em> API. It will do nothing on
 * dedicated servers (i.e. <tt>API.manual</tt> will be <tt>null</tt>).
 */
public final class ManualAPI {
    /**
     * Get the image renderer for the specified image path.
     * <p>
     * This will look for {@link RendererProvider}s registered for a prefix in the
     * specified path. If there is no match, or the matched content provider
     * does not provide a renderer, this will return <tt>null</tt>.
     *
     * @param path the path to the image to get the renderer for.
     * @return the custom renderer for that path.
     */
    @Nullable
    public static ContentRenderer imageFor(final String path) {
        if (API.manualAPI != null) {
            return API.manualAPI.imageFor(path);
        }
        return null;
    }

    // ----------------------------------------------------------------------- //

    /**
     * Look up the documentation path for the specified item stack.
     *
     * @param stack the stack to find the documentation path for.
     * @return the path to the page, <tt>null</tt> if none is known.
     */
    @Nullable
    public static String pathFor(final ItemStack stack) {
        if (API.manualAPI != null) {
            return API.manualAPI.pathFor(stack);
        }
        return null;
    }

    /**
     * Look up the documentation for the specified block in the world.
     *
     * @param world the world containing the block.
     * @param pos   the position of the block.
     * @param side  the face of the block.
     * @return the path to the page, <tt>null</tt> if none is known.
     */
    @Nullable
    public static String pathFor(final World world, final BlockPos pos, final Direction side) {
        if (API.manualAPI != null) {
            return API.manualAPI.pathFor(world, pos, side);
        }
        return null;
    }

    /**
     * Get the content of the documentation page at the specified location.
     *
     * @param path the path of the page to get the content of.
     * @return the content of the page, or <tt>null</tt> if none exists.
     */
    @Nullable
    public static Iterable<String> contentFor(final String path) {
        if (API.manualAPI != null) {
            return API.manualAPI.contentFor(path);
        }
        return null;
    }

    // ----------------------------------------------------------------------- //

    /**
     * Open the manual for the specified player.
     * <p>
     * If you wish to display a specific page, call {@link #navigate(String)}
     * after this function returns, with the path to the page to show.
     */
    public static void open() {
        if (API.manualAPI != null) {
            API.manualAPI.open();
        }
    }

    /**
     * Reset the history of the manual.
     */
    public static void reset() {
        if (API.manualAPI != null) {
            API.manualAPI.reset();
        }
    }

    /**
     * Navigate to a page in the manual.
     *
     * @param path the path to navigate to.
     */
    public static void navigate(final String path) {
        if (API.manualAPI != null) {
            API.manualAPI.navigate(path);
        }
    }

    // ----------------------------------------------------------------------- //

    private ManualAPI() {
    }
}
