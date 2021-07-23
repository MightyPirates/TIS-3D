package li.cil.tis3d.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class WorldUtils {
    /**
     * Check whether a block is within a loaded chunk.
     * <p>
     * This differs from {@link Level#isLoaded(BlockPos)} in such that this treats out of bounds chunks as loaded.
     *
     * @param level the world to check in.
     * @param pos   the block position to check at.
     * @return whether the block is loaded.
     */
    public static boolean isLoaded(final Level level, final BlockPos pos) {
        final ChunkPos chunkPos = new ChunkPos(pos);
        return level.getChunkSource().hasChunk(chunkPos.x, chunkPos.z);
    }

    // --------------------------------------------------------------------- //

    private WorldUtils() {
    }
}
