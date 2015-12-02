package li.cil.tis3d.api;

/**
 * The ports that can be available on a module.
 * <p>
 * For the top and bottom faces, {@link #UP} points towards {@link Face#Z_POS}.
 */
public enum Port {
    LEFT,
    RIGHT,
    UP,
    DOWN;

    /**
     * All possible enum values for quick indexing.
     */
    public static final Port[] VALUES = Port.values();

    /**
     * Mapping ports to their opposites (by <tt>ordinal()</tt>).
     */
    public static final Port[] OPPOSITES = new Port[]{RIGHT, LEFT, DOWN, UP};

    /**
     * The the opposite port to this one.
     *
     * @return the opposite port.
     * @see #OPPOSITES
     */
    public Port getOpposite() {
        return OPPOSITES[ordinal()];
    }
}
