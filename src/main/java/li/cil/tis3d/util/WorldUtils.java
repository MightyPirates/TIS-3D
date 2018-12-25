package li.cil.tis3d.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import javax.annotation.Nullable;

public final class WorldUtils {
    @Nullable
    public static BlockEntity getTileEntityThreadsafe(final BlockView world, final BlockPos pos) {
        return world instanceof World ? ((World) world).getChunk(pos).getBlockEntity(pos, WorldChunk.AccessType.GET) : world.getBlockEntity(pos);
    }

    private WorldUtils() {
    }
}
