/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.api.util;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

/**
 * Utility class for coordinate transformation related operations.
 */
public final class TransformUtil {
    /**
     * Project a hit position on the surface of a block to a UV coordinate on
     * the that side.
     *
     * @param side   the face to project the hit onto, in world space.
     * @param hitPos the hit position to project, in block local coordinates.
     * @return the projected UV coordinate, with the Z component being 0.
     */
    public static Vec3 hitToUV(final Direction side, final Vec3 hitPos) {
        return switch (side) {
            case DOWN -> new Vec3(1 - hitPos.x, hitPos.z, 0);
            case UP -> new Vec3(1 - hitPos.x, 1 - hitPos.z, 0);
            case NORTH -> new Vec3(1 - hitPos.x, 1 - hitPos.y, 0);
            case SOUTH -> new Vec3(hitPos.x, 1 - hitPos.y, 0);
            case WEST -> new Vec3(hitPos.z, 1 - hitPos.y, 0);
            case EAST -> new Vec3(1 - hitPos.z, 1 - hitPos.y, 0);
        };
    }

    /**
     * Project a hit position on the surface of a block to a UV coordinate on
     * the that side, taking into account potential rotation of the block
     * around the Y axis (up being south).
     *
     * @param side   the face to project the hit onto, in world space.
     * @param facing the rotation of the block, in world space.
     * @param hitPos the hit position to project, in block local coordinates.
     * @return the projected UV coordinate, with the Z component being 0.
     * @see Port#fromDirection(Direction)
     */
    public static Vec3 hitToUV(final Direction side, final Port facing, final Vec3 hitPos) {
        final Vec3 uv = hitToUV(side, hitPos);
        return switch (side) {
            case DOWN -> switch (facing) {
                case LEFT -> new Vec3(uv.y, 1 - uv.x, 0);
                case RIGHT -> new Vec3(1 - uv.y, uv.x, 0);
                case UP -> uv;
                case DOWN -> new Vec3(1 - uv.x, 1 - uv.y, 0);
            };
            case UP -> switch (facing) {
                case LEFT -> new Vec3(1 - uv.y, uv.x, 0);
                case RIGHT -> new Vec3(uv.y, 1 - uv.x, 0);
                case UP -> uv;
                case DOWN -> new Vec3(1 - uv.x, 1 - uv.y, 0);
            };
            default -> uv;
        };
    }

    /**
     * Map a face from a casing's local space to world space.
     *
     * @param face     the face in the casing's local space.
     * @param rotation the rotation of the casing.
     * @return the direction the face points in, in world space.
     */
    public static Direction toWorld(final Face face, final Rotation rotation) {
        return rotation.rotate(Face.toDirection(face));
    }

    /**
     * Map a face from world space to a casing's local space.
     *
     * @param side     the face in world space.
     * @param rotation the rotation of the casing.
     * @return the face in the casing's local space.
     */
    public static Face toLocal(final Direction side, final Rotation rotation) {
        return Face.fromDirection(inverse(rotation).rotate(side));
    }

    /**
     * Map a port from a casing's local space to world space.
     *
     * @param face     the face the port is on, in the casing's local space.
     * @param port     the port in the casing's local space.
     * @param rotation the rotation of the casing.
     * @return the port in world space.
     */
    public static Port toWorld(final Face face, final Port port, final Rotation rotation) {
        return switch (face) {
            case Y_POS -> port.rotated(steps(rotation));
            case Y_NEG -> port.rotated(-steps(rotation));
            default -> port;
        };
    }

    /**
     * Map a port from world space to a casing's local space.
     *
     * @param side     the face the port is on, in world space.
     * @param port     the port in world space.
     * @param rotation the rotation of the casing.
     * @return the port in the casing's local space.
     */
    public static Port toLocal(final Direction side, final Port port, final Rotation rotation) {
        return switch (side) {
            case UP -> port.rotated(-steps(rotation));
            case DOWN -> port.rotated(steps(rotation));
            default -> port;
        };
    }

    // --------------------------------------------------------------------- //

    private static int steps(final Rotation rotation) {
        return switch (rotation) {
            case NONE -> 0;
            case CLOCKWISE_90 -> 1;
            case CLOCKWISE_180 -> 2;
            case COUNTERCLOCKWISE_90 -> 3;
        };
    }

    private static Rotation inverse(final Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            default -> rotation;
        };
    }

    // --------------------------------------------------------------------- //

    private TransformUtil() {
    }
}
