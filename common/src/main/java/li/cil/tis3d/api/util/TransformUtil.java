package li.cil.tis3d.api.util;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * Utility class for coordinate transformation related operations.
 */
public final class TransformUtil {
    /**
     * Project a hit position on the surface of a block to a UV coordinate on
     * the that side.
     *
     * @param face   the face to project the hit onto.
     * @param hitPos the hit position to project, in block local coordinates.
     * @return the projected UV coordinate, with the Z component being 0.
     */
    public static Vec3 hitToUV(final Face face, final Vec3 hitPos) {
        return switch (face) {
            case Y_NEG -> new Vec3(1 - hitPos.x, hitPos.z, 0);
            case Y_POS -> new Vec3(1 - hitPos.x, 1 - hitPos.z, 0);
            case Z_NEG -> new Vec3(1 - hitPos.x, 1 - hitPos.y, 0);
            case Z_POS -> new Vec3(hitPos.x, 1 - hitPos.y, 0);
            case X_NEG -> new Vec3(hitPos.z, 1 - hitPos.y, 0);
            case X_POS -> new Vec3(1 - hitPos.z, 1 - hitPos.y, 0);
        };
    }

    /**
     * Project a hit position on the surface of a block to a UV coordinate on
     * the that side, taking into account potential rotation of the block
     * around the Y axis (up being south).
     *
     * @param face   the face to project the hit onto.
     * @param facing the rotation of the block.
     * @param hitPos the hit position to project, in block local coordinates.
     * @return the projected UV coordinate, with the Z component being 0.
     * @see Face#fromDirection(Direction)
     * @see Port#fromDirection(Direction)
     */
    public static Vec3 hitToUV(final Face face, final Port facing, final Vec3 hitPos) {
        final Vec3 uv = hitToUV(face, hitPos);
        return switch (face) {
            case Y_NEG -> switch (facing) {
                case LEFT -> new Vec3(uv.y, 1 - uv.x, 0);
                case RIGHT -> new Vec3(1 - uv.y, uv.x, 0);
                case UP -> uv;
                case DOWN -> new Vec3(1 - uv.x, 1 - uv.y, 0);
            };
            case Y_POS -> switch (facing) {
                case LEFT -> new Vec3(1 - uv.y, uv.x, 0);
                case RIGHT -> new Vec3(uv.y, 1 - uv.x, 0);
                case UP -> uv;
                case DOWN -> new Vec3(1 - uv.x, 1 - uv.y, 0);
            };
            default -> uv;
        };
    }

    // --------------------------------------------------------------------- //

    private TransformUtil() {
    }
}
