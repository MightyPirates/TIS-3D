/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.api.machine;

import net.minecraft.core.Direction;

/**
 * Enumeration over the faces of a {@link Casing}, in that casing's local space.
 * <p>
 * Casings can be rotated, and store everything per face and per port as though they were not. World
 * space is {@link Direction} throughout; this type is local space and nothing else. Map between the
 * two with {@link Casing#toWorld(Face)} and {@link Casing#toLocal(Direction)}.
 */
public enum Face {
    Y_NEG,
    Y_POS,
    Z_NEG,
    Z_POS,
    X_NEG,
    X_POS;

    // --------------------------------------------------------------------- //

    /**
     * All possible enum values for quick indexing.
     */
    public static final Face[] VALUES = Face.values();

    /**
     * Cached because {@link Direction#values()} clones its array and this is used in render logic.
     */
    private static final Direction[] DIRECTIONS = Direction.values();

    // --------------------------------------------------------------------- //

    /**
     * Convert a direction to a face, without applying any rotation.
     * <p>
     * This is the raw ordinal correspondence, not a world to local mapping: it is only correct for
     * an unrotated casing. Use {@link Casing#toLocal(Direction)} instead.
     *
     * @param side the facing to convert.
     * @return the {@link Face} representing that facing.
     */
    public static Face fromDirection(final Direction side) {
        return VALUES[side.ordinal()];
    }

    /**
     * Convert a face to a direction, without applying any rotation.
     * <p>
     * This is the raw ordinal correspondence, not a local to world mapping: it is only correct for
     * an unrotated casing. Use {@link Casing#toWorld(Face)} instead.
     *
     * @param face the face to convert.
     * @return the {@link Direction} representing that facing.
     */
    public static Direction toDirection(final Face face) {
        return DIRECTIONS[face.ordinal()];
    }
}
