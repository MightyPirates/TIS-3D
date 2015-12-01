package li.cil.tis3d.api;

import net.minecraft.util.EnumFacing;

/**
 * Enumeration over the faces of a {@link Casing}.
 */
public enum Face {
    Y_NEG,
    Y_POS,
    Z_NEG,
    Z_POS,
    X_NEG,
    X_POS;

    public static final Face[] VALUES = Face.values();

    public static final Face[] OPPOSITES = new Face[]{Y_POS, Y_NEG, Z_POS, Z_NEG, X_POS, X_NEG};

    public Face getOpposite() {
        return OPPOSITES[ordinal()];
    }

    public static Face fromEnumFacing(final EnumFacing facing) {
        return VALUES[facing.ordinal()];
    }

    public static EnumFacing toEnumFacing(final Face face) {
        return EnumFacing.VALUES[face.ordinal()];
    }
}
