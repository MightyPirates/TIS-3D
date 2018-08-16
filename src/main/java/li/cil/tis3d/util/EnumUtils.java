package li.cil.tis3d.util;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.Constants;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;

/**
 * Utility method for wrapping enum serialization against exceptions.
 */
public final class EnumUtils {
    public static <T extends Enum<T>> T readFromNBT(final Class<T> clazz, final String tagName, final NBTTagCompound nbt) {
        if (nbt.hasKey(tagName, Constants.NBT.TAG_STRING)) {
            // Backwards compatibility.
            try {
                return Enum.valueOf(clazz, nbt.getString(tagName));
            } catch (final IllegalArgumentException e) {
                // This can only happen if someone messes with the save.
                LogManager.getLogger(API.MOD_ID).warn("Broken save, enum value is invalid.", e);
                return clazz.getEnumConstants()[0];
            }
        } else {
            return clazz.getEnumConstants()[nbt.getByte(tagName)];
        }
    }

    public static <T extends Enum<T>> void writeToNBT(final Enum<T> value, final String tagName, final NBTTagCompound nbt) {
        nbt.setByte(tagName, (byte) value.ordinal());
    }

    // --------------------------------------------------------------------- //

    private EnumUtils() {
    }
}
