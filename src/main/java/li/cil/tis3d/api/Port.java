package li.cil.tis3d.api;

/**
 * The ports that can be available on a module.
 */
public enum Port {
    LEFT,
    RIGHT,
    UP,
    DOWN;

    // --------------------------------------------------------------------- //

    /**
     * The the opposite port to this one.
     *
     * @return the opposite port.
     * @see #OPPOSITES
     */
    public Port getOpposite() {
        return OPPOSITES[ordinal()];
    }

    /**
     * Get the next port in clockwise rotation.
     *
     * @return the port right to this port.
     */
    public Port rotated() {
        return ORDERED[(ordinal() + 1) % ORDERED.length];
    }

    /**
     * Get the n-th next port in clockwise rotation.
     *
     * @param steps how many steps to make.
     * @return the n-th right neighbor to this port.
     */
    public Port rotated(final int steps) {
        final int clampedSteps = Math.abs(steps) % 4;
        final int orderedSteps = steps > 0 ? clampedSteps : (4 - clampedSteps);
        return orderedSteps > 0 ? rotated().rotated(orderedSteps - 1) : this;
    }

    // --------------------------------------------------------------------- //

    /**
     * All possible enum values for quick indexing.
     */
    public static final Port[] VALUES = Port.values();

    /**
     * Mapping ports to their opposites (by <tt>ordinal()</tt>).
     */
    public static final Port[] OPPOSITES = new Port[]{RIGHT, LEFT, DOWN, UP};

    /**
     * Listing of all ports in clockwise order.
     */
    public static final Port[] ORDERED = new Port[]{UP, RIGHT, DOWN, LEFT};

    /**
     * Mapping of port id to rotation relative to {@link #UP}.
     */
    public static final int[] ROTATION = new int[]{3, 1, 0, 2};
}
