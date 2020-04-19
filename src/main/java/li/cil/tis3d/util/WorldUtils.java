package li.cil.tis3d.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.WorldChunk;

import javax.annotation.Nullable;

public final class WorldUtils {
    @Nullable
    public static BlockEntity getBlockEntityThreadsafe(final BlockView world, final BlockPos pos) {
        return world instanceof World ? ((WorldChunk)((World)world).getChunk(pos)).getBlockEntity(pos, WorldChunk.CreationType.CHECK) : world.getBlockEntity(pos);
    }

    public static boolean isBlockLoaded(final IWorld iWorld, final BlockPos pos) {
        final ChunkPos chunkPos = new ChunkPos(pos);
        return iWorld.isChunkLoaded(chunkPos.x, chunkPos.z);
    }

    private WorldUtils() {
    }
}
