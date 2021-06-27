package li.cil.manual.api.provider;

import li.cil.manual.api.Manual;
import li.cil.manual.api.util.ComparableRegistryEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

/**
 * Allows providing paths for item stacks and blocks in the world.
 * <p>
 * This is used for generating NEI usage pages with a button opening the manual
 * on the page at the specified path, or for opening the manual when held in
 * hand and sneak-activating a block in the world.
 * <p>
 * This way you can easily make entries in your documentation available the
 * same way OpenComputers does it itself.
 * <p>
 * Note that you can use the special variable {@link Manual#LANGUAGE_KEY} in your
 * paths for language agnostic paths. These will be resolved to the currently
 * set language, falling back to {@link Manual#FALLBACK_LANGUAGE}, during actual
 * content lookup.
 */
@OnlyIn(Dist.CLIENT)
public interface PathProvider extends ComparableRegistryEntry<PathProvider> {
    /**
     * Get the path to the documentation page for the provided item stack.
     * <p>
     * Return {@code null} if there is no known page for this item, allowing
     * other providers to be queried.
     *
     * @param stack the stack to get the documentation path to.
     * @return the path to the page, {@code null} if none is known.
     */
    Optional<String> pathFor(final ItemStack stack);

    /**
     * Get the path to the documentation page for the provided block.
     * <p>
     * Return {@code null} if there is no known page for this item, allowing
     * other providers to be queried.
     *
     * @param world the world containing the block.
     * @param pos   the position coordinate of the block.
     * @param face  the face of the block.
     * @return the path to the page, {@code null} if none is known.
     */
    Optional<String> pathFor(final World world, final BlockPos pos, final Direction face);
}
