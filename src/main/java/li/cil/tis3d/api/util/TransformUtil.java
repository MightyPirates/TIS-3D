package li.cil.tis3d.api.util;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

/**
 * Utility class for coordinate transformation related operations.
 */
public final class TransformUtil {
    /**
     * Project a hit position on the surface of a block to a UV coordinate on
     * the that side.
     *
     * @param hitPos the hit position to project.
     * @return the projected UV coordinate, with the Z component being 0.
     */
    @Nullable
    public static Vec3d hitToUV(final Face face, final Vec3d hitPos) {
        switch (face) {
            case Y_NEG:
                return new Vec3d(1 - hitPos.xCoord, hitPos.zCoord, 0);
            case Y_POS:
                return new Vec3d(1 - hitPos.xCoord, 1 - hitPos.zCoord, 0);
            case Z_NEG:
                return new Vec3d(1 - hitPos.xCoord, 1 - hitPos.yCoord, 0);
            case Z_POS:
                return new Vec3d(hitPos.xCoord, 1 - hitPos.yCoord, 0);
            case X_NEG:
                return new Vec3d(hitPos.zCoord, 1 - hitPos.yCoord, 0);
            case X_POS:
                return new Vec3d(1 - hitPos.zCoord, 1 - hitPos.yCoord, 0);
        }
        return null;
    }

    /**
     * Project a hit position on the surface of a block to a UV coordinate on
     * the that side, taking into account potential rotation of the block
     * around the Y axis (up being south).
     *
     * @param hitPos the hit position to project.
     * @return the projected UV coordinate, with the Z component being 0.
     * @see Face#fromEnumFacing(EnumFacing)
     * @see Port#fromEnumFacing(EnumFacing)
     */
    @SuppressWarnings("SuspiciousNameCombination")
    @Nullable
    public static Vec3d hitToUV(final Face face, final Port facing, final Vec3d hitPos) {
        final Vec3d uv = hitToUV(face, hitPos);
        if (uv == null) {
            return null;
        }

        switch (face) {
            case Y_NEG:
                switch (facing) {
                    case LEFT:
                        return new Vec3d(uv.yCoord, 1 - uv.xCoord, 0);
                    case RIGHT:
                        return new Vec3d(1 - uv.yCoord, uv.xCoord, 0);
                    case UP:
                        return uv;
                    case DOWN:
                        return new Vec3d(1 - uv.xCoord, 1 - uv.yCoord, 0);
                }
                break;
            case Y_POS:
                switch (facing) {
                    case LEFT:
                        return new Vec3d(1 - uv.yCoord, uv.xCoord, 0);
                    case RIGHT:
                        return new Vec3d(uv.yCoord, 1 - uv.xCoord, 0);
                    case UP:
                        return uv;
                    case DOWN:
                        return new Vec3d(1 - uv.xCoord, 1 - uv.yCoord, 0);
                }
                break;
        }
        return uv;
    }

    // --------------------------------------------------------------------- //

    private TransformUtil() {
    }
}
