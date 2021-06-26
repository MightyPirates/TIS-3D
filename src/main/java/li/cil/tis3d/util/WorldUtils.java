package li.cil.tis3d.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public final class WorldUtils {
    /**
     * Check whether a block is within a loaded chunk.
     * <p>
     * This differs from {@link World#isLoaded(BlockPos)} in such that this treats out of bounds chunks as loaded.
     *
     * @param world the world to check in.
     * @param pos   the block position to check at.
     * @return whether the block is loaded.
     */
    public static boolean isLoaded(final World world, final BlockPos pos) {
        final ChunkPos chunkPos = new ChunkPos(pos);
        return world.getChunkSource().hasChunk(chunkPos.x, chunkPos.z);
    }

    // --------------------------------------------------------------------- //

    private WorldUtils() {
    }
}
