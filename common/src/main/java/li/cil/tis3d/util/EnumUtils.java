/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.util;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Utility methods for enum serialization by ordinal.
 */
public final class EnumUtils {
    public static <T extends Enum<T>> T load(final Class<T> clazz, final String tagName, final ValueInput input) {
        return clazz.getEnumConstants()[input.getByteOr(tagName, (byte) 0)];
    }

    public static <T extends Enum<T>> void save(final Enum<T> value, final String tagName, final ValueOutput output) {
        output.putByte(tagName, (byte) value.ordinal());
    }

    // --------------------------------------------------------------------- //

    private EnumUtils() {
    }
}
