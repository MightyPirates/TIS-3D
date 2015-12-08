package li.cil.tis3d.util;

import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Custom ray-tracing implementation for ray-block tests, to allow custom
 * filter methods for blocks to take into account.
 */
public final class Raytracing {
    @FunctionalInterface
    public interface CollisionDetector {
        MovingObjectPosition intersect(final World world, final int x, final int y, final int z, final Vec3 start, final Vec3 end);
    }

    /**
     * Standard callback for {@link #raytrace(World, Vec3, Vec3, CollisionDetector)},
     * only checks blocks that have a bounding box and are not liquids.
     *
     * @param world the world to perform the intersection check in.
     * @param x     the x position of the block to perform the intersection check with.
     * @param y     the y position of the block to perform the intersection check with.
     * @param z     the z position of the block to perform the intersection check with.
     * @param start the start of the line to intersect the block with.
     * @param end   the end of the line to intersect the block with.
     * @return hit information on the intersect, or <tt>null</tt> if there was none.
     */
    public static MovingObjectPosition intersectIgnoringLiquids(final World world, final int x, final int y, final int z, final Vec3 start, final Vec3 end) {
        final Block block = world.getBlock(x, y, z);
        if (block.getCollisionBoundingBoxFromPool(world, x, y, z) != null && block.canCollideCheck(world.getBlockMetadata(x, y, z), false)) {
            return block.collisionRayTrace(world, x, y, z, start, end);
        }
        return null;
    }

    /**
     * Checks only blocks that have a bounding box and are not see-through.
     *
     * @param world the world to perform the intersection check in.
     * @param x     the x position of the block to perform the intersection check with.
     * @param y     the y position of the block to perform the intersection check with.
     * @param z     the z position of the block to perform the intersection check with.
     * @param start the start of the line to intersect the block with.
     * @param end   the end of the line to intersect the block with.
     * @return hit information on the intersect, or <tt>null</tt> if there was none.
     */
    public static MovingObjectPosition intersectIgnoringTransparent(final World world, final int x, final int y, final int z, final Vec3 start, final Vec3 end) {
        final Block block = world.getBlock(x, y, z);
        if (!block.getMaterial().blocksMovement() || !block.getMaterial().isOpaque() || !block.getMaterial().getCanBlockGrass()) {
            return null;
        }
        if (block.getCollisionBoundingBoxFromPool(world, x, y, z) != null && block.canCollideCheck(world.getBlockMetadata(x, y, z), false)) {
            return block.collisionRayTrace(world, x, y, z, start, end);
        }
        return null;
    }

    // --------------------------------------------------------------------- //

    /**
     * Trace along the specified line, testing for collision with blocks along the way.
     * <p>
     * Uses the default intersection logic defined in {@link #intersectIgnoringLiquids(World, int, int, int, Vec3, Vec3)}.
     *
     * @param world the world to shoot the ray in.
     * @param start the start of the line to trace.
     * @param end   the end of the line to trace.
     * @return the first detected hit, or <tt>null</tt> if there was none.
     */
    public static MovingObjectPosition raytrace(final World world, final Vec3 start, final Vec3 end) {
        return raytrace(world, start, end, Raytracing::intersectIgnoringLiquids);
    }

    /**
     * Trace along the specified line, testing for collision with blocks along the way.
     *
     * @param world    the world to shoot the ray in.
     * @param start    the start of the line to trace.
     * @param end      the end of the line to trace.
     * @param callback the method to call for each potential hit to perform collision logic.
     * @return the first detected hit, or <tt>null</tt> if there was none.
     */
    public static MovingObjectPosition raytrace(final World world, final Vec3 start, final Vec3 end, final CollisionDetector callback) {
        // Adapted from http://jsfiddle.net/wivlaro/mkaWf/6/

        final int startPosX = MathHelper.floor_double(start.xCoord);
        final int startPosY = MathHelper.floor_double(start.yCoord);
        final int startPosZ = MathHelper.floor_double(start.zCoord);

        final int endPosX = MathHelper.floor_double(end.xCoord);
        final int endPosY = MathHelper.floor_double(end.yCoord);
        final int endPosZ = MathHelper.floor_double(end.zCoord);

        final int stepX = endPosX > startPosX ? 1 : endPosX < startPosX ? -1 : 0;
        final int stepY = endPosY > startPosY ? 1 : endPosY < startPosY ? -1 : 0;
        final int stepZ = endPosZ > startPosZ ? 1 : endPosZ < startPosZ ? -1 : 0;

        // Planes for each axis that we will next cross.
        final int gxp = startPosX + (endPosX > startPosX ? 1 : 0);
        final int gyp = startPosY + (endPosY > startPosY ? 1 : 0);
        final int gzp = startPosZ + (endPosZ > startPosZ ? 1 : 0);

        // Only used for multiplying up the error margins.
        final double vx = end.xCoord == start.xCoord ? 1 : end.xCoord - start.xCoord;
        final double vy = end.yCoord == start.yCoord ? 1 : end.yCoord - start.yCoord;
        final double vz = end.zCoord == start.zCoord ? 1 : end.zCoord - start.zCoord;

        // Error is normalized to vx * vy * vz so we only have to multiply up.
        final double vxvy = vx * vy;
        final double vxvz = vx * vz;
        final double vyvz = vy * vz;

        // Error from the next plane accumulators, scaled up by vx*vy*vz.
        final double scaledErrorX = stepX * vyvz;
        final double scaledErrorY = stepY * vxvz;
        final double scaledErrorZ = stepZ * vxvy;

        double errorX = (gxp - start.xCoord) * vyvz;
        double errorY = (gyp - start.yCoord) * vxvz;
        double errorZ = (gzp - start.zCoord) * vxvy;

        int currentPosX = startPosX;
        int currentPosY = startPosY;
        int currentPosZ = startPosZ;

        int emergencyExit = 200;
        while (--emergencyExit > 0) {
            // Check if we're colliding with the block.
            final MovingObjectPosition hit = callback.intersect(world, currentPosX, currentPosY, currentPosZ, start, end);
            if (hit != null && hit.typeOfHit != MovingObjectPosition.MovingObjectType.MISS) {
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
