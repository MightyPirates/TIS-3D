package li.cil.tis3d.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;

public final class WorldUtils {
    @Nullable
    public static TileEntity getTileEntityThreadsafe(final IBlockReader world, final BlockPos pos) {
        return world instanceof ChunkCache ? ((ChunkCache) world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : world.getTileEntity(pos);
    }

    private WorldUtils() {
    }
}
