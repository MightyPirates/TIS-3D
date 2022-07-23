package li.cil.tis3d.api.module;

import li.cil.tis3d.api.API;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Redstone input providers can be queried to compute simple redstone input into blocks.
 * <p>
 * This is used by the redstone module to compute its current input value. The default
 * implementation reads the regular Minecraft redstone signal, but other mods may want
 * to provide custom providers for custom redstone signal transportation methods, such
 * as cables.
 * <p>
 * Additional providers may be registered with the {@link net.minecraft.core.Registry}
 * <tt>tis3d:redstone_inputs</tt>.
 */
public interface RedstoneInputProvider {
    /**
     * The registry name of the registry holding redstone input providers.
     */
    ResourceKey<Registry<RedstoneInputProvider>> REGISTRY = ResourceKey.createRegistryKey(new ResourceLocation(API.MOD_ID, "redstone_input_provider"));

    /**
     * Get the redstone level provided to the specified face of a block at the specified position.
     *
     * @param level    the level containing the block in question.
     * @param position the position of the block.
     * @param face     the face of the block.
     * @return the redstone level going into the face of the block.
     */
    int getInput(final Level level, final BlockPos position, final Direction face);
}
