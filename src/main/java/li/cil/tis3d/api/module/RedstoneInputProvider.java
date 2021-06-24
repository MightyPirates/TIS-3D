package li.cil.tis3d.api.module;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Redstone input providers can be queried to compute simple redstone input into blocks.
 * <p>
 * This is used by the redstone module to compute its current input value. The default
 * implementation reads the regular Minecraft redstone signal, but other mods may want
 * to provide custom providers for custom redstone signal transportation methods, such
 * as cables.
 * <p>
 * Additional providers may be registered with the {@link Registry} <tt>tis3d:redstone_inputs</tt>.
 */
public interface RedstoneInputProvider extends IForgeRegistryEntry<RedstoneInputProvider> {
    /**
     * Get the redstone level provided to the specified face of a block at the specified position.
     *
     * @param world the world containing the block in question.
     * @param pos   the position of the block.
     * @param face  the face of the block.
     * @return the redstone level going into the face of the block.
     */
    int getInput(final World world, final BlockPos pos, final Direction face);
}
