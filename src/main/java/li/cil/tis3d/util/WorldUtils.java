package li.cil.tis3d.util;

import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import javax.annotation.Nullable;

public final class WorldUtils {
    /**
     * Tries to get a tile entity in a thread-safe manner (avoids causing chunks to be generated).
     *
     * @param world the world to get the tile entity from.
     * @param pos   the position to get the tile entity at.
     * @return a tile entity, or <code>null</code>.
     */
    @Nullable
    public static TileEntity getTileEntityThreadsafe(final IBlockReader world, final BlockPos pos) {
        if (world instanceof ChunkRenderCache) {
            final ChunkRenderCache renderCache = (ChunkRenderCache) world;
            return renderCache.getBlockEntity(pos, Chunk.CreateEntityType.CHECK);
        }
        return world.getBlockEntity(pos);
    }

    /**
     * Check whether a block is within a loaded chunk.
     *
     * @param world the world to check in.
     * @param pos   the block position to check at.
     * @return whether the block is loaded.
     */
    public static boolean isBlockLoaded(final IWorldReader world, final BlockPos pos) {
        final ChunkPos chunkPos = new ChunkPos(pos);
        return world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false) != null;
    }

    // --------------------------------------------------------------------- //

    private WorldUtils() {
    }
}
