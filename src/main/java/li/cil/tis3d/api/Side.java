package li.cil.tis3d.api;

/**
 * The ports that can be available on a module.
 * <p>
 * For the top and bottom faces, {@link #UP} equals {@link Face#Z_POS}.
 */
public enum Side {
    LEFT,
    RIGHT,
    UP,
    DOWN;

    public static final Side[] VALUES = Side.values();

    public static final Side[] OPPOSITES = new Side[]{RIGHT, LEFT, DOWN, UP};

    public Side getOpposite() {
        return OPPOSITES[ordinal()];
    }
}
