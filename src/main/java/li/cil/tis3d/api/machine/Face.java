package li.cil.tis3d.api.machine;

import net.minecraft.core.Direction;

/**
 * Enumeration over the faces of a {@link Casing}.
 * <p>
 * Mainly to avoid Minecraft specific types in the API where possible. Just in
 * case things get changed around again like has happened the last few major
 * version bumps.
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
     * The the opposite face to this one.
     *
     * @return the opposite port.
     * @see #OPPOSITES
     */
    public Face getOpposite() {
        return OPPOSITES[ordinal()];
    }

    // --------------------------------------------------------------------- //

    /**
     * All possible enum values for quick indexing.
     */
    public static final Face[] VALUES = Face.values();

    /**
     * Mapping faces to their opposites (by <tt>ordinal()</tt>).
     */
    public static final Face[] OPPOSITES = new Face[]{Y_POS, Y_NEG, Z_POS, Z_NEG, X_POS, X_NEG};

    // --------------------------------------------------------------------- //

    /**
     * Convert a facing from Forge's format to our internal format.
     *
     * @param facing the facing to convert.
     * @return the {@link Face} representing that facing.
     */
    public static Face fromDirection(final Direction facing) {
        return VALUES[facing.ordinal()];
    }

    /**
     * Convert a facing from our internal format to Forge's format.
     *
     * @param face the face to convert.
     * @return the {@link Direction} representing that facing.
     */
    public static Direction toDirection(final Face face) {
        return Direction.values()[face.ordinal()];
    }
}
