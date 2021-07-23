package li.cil.tis3d.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Custom ray-tracing implementation for ray-block tests, to allow custom
 * filter methods for blocks to take into account.
 */
public final class Raytracing {
    @FunctionalInterface
    public interface CollisionDetector {
        @Nullable
        HitResult intersect(final Level world, final BlockPos position, final Vec3 start, final Vec3 end);
    }

    /**
     * Checks only blocks that have a bounding box and are not see-through.
     *
     * @param world    the world to perform the intersection check in.
     * @param position the position of the block to perform the intersection check with.
     * @param start    the start of the line to intersect the block with.
     * @param end      the end of the line to intersect the block with.
     * @return hit information on the intersect, or {@code null} if there was none.
     */
    @Nullable
    public static HitResult intersectIgnoringTransparent(final Level world, final BlockPos position, final Vec3 start, final Vec3 end) {
        final BlockState state = world.getBlockState(position);
        if (state.isSolidRender(world, position)) {
            final VoxelShape shape = state.getOcclusionShape(world, position);
            if (!shape.isEmpty()) {
                return shape.clip(start, end, position);
            }
        }
        return null;
    }

    // --------------------------------------------------------------------- //

    /**
     * Trace along the specified line, testing for collision with blocks along the way.
     *
     * @param world    the world to shoot the ray in.
     * @param start    the start of the line to trace.
     * @param end      the end of the line to trace.
     * @param callback the method to call for each potential hit to perform collision logic.
     * @return the first detected hit, or {@code null} if there was none.
     */
    @Nullable
    public static HitResult raytrace(final Level world, final Vec3 start, final Vec3 end, final CollisionDetector callback) {
        // Adapted from http://jsfiddle.net/wivlaro/mkaWf/6/

        final int startPosX = Mth.floor(start.x);
        final int startPosY = Mth.floor(start.y);
        final int startPosZ = Mth.floor(start.z);

        final int endPosX = Mth.floor(end.x);
        final int endPosY = Mth.floor(end.y);
        final int endPosZ = Mth.floor(end.z);

        final int stepX = Integer.compare(endPosX, startPosX);
        final int stepY = Integer.compare(endPosY, startPosY);
        final int stepZ = Integer.compare(endPosZ, startPosZ);

        // Planes for each axis that we will next cross.
        final int gxp = startPosX + (endPosX > startPosX ? 1 : 0);
        final int gyp = startPosY + (endPosY > startPosY ? 1 : 0);
        final int gzp = startPosZ + (endPosZ > startPosZ ? 1 : 0);

        // Only used for multiplying up the error margins.
        final double vx = end.x == start.x ? 1 : end.x - start.x;
        final double vy = end.y == start.y ? 1 : end.y - start.y;
        final double vz = end.z == start.z ? 1 : end.z - start.z;

        // Error is normalized to vx * vy * vz so we only have to multiply up.
        final double vxvy = vx * vy;
        final double vxvz = vx * vz;
        final double vyvz = vy * vz;

        // Error from the next plane accumulators, scaled up by vx*vy*vz.
        final double scaledErrorX = stepX * vyvz;
        final double scaledErrorY = stepY * vxvz;
        final double scaledErrorZ = stepZ * vxvy;

        double errorX = (gxp - start.x) * vyvz;
        double errorY = (gyp - start.y) * vxvz;
        double errorZ = (gzp - start.z) * vxvy;

        int currentPosX = startPosX;
        int currentPosY = startPosY;
        int currentPosZ = startPosZ;

        int emergencyExit = 200;
        while (--emergencyExit > 0) {
            // Check if we're colliding with the block.
            final BlockPos position = new BlockPos(currentPosX, currentPosY, currentPosZ);
            final HitResult hit = callback.intersect(world, position, start, end);
            if (hit != null && hit.getType() != HitResult.Type.MISS) {
                return hit;
            }

            // Have we reached the end point without hitting anything?
            if (currentPosX == endPosX && currentPosY == endPosY && currentPosZ == endPosZ) {
                return null;
            }

            // Which plane do we cross first?
            final double xr = Math.abs(errorX);
            final double yr = Math.abs(errorY);
            final double zr = Math.abs(errorZ);

            if (stepX != 0 && (stepY == 0 || xr < yr) && (stepZ == 0 || xr < zr)) {
                currentPosX += stepX;
                errorX += scaledErrorX;
            } else if (stepY != 0 && (stepZ == 0 || yr < zr)) {
                currentPosY += stepY;
                errorY += scaledErrorY;
            } else if (stepZ != 0) {
                currentPosZ += stepZ;
                errorZ += scaledErrorZ;
            }
        }

        return null;
    }

    // --------------------------------------------------------------------- //

    private Raytracing() {
    }
}
