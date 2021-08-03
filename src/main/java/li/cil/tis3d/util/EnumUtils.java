package li.cil.tis3d.util;

import li.cil.tis3d.api.API;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.LogManager;

/**
 * Utility method for wrapping enum serialization against exceptions.
 */
public final class EnumUtils {
    public static <T extends Enum<T>> T load(final Class<T> clazz, final String tagName, final CompoundTag tag) {
        if (tag.contains(tagName, Constants.NBT.TAG_STRING)) {
            // Backwards compatibility.
            try {
                return Enum.valueOf(clazz, tag.getString(tagName));
            } catch (final IllegalArgumentException e) {
                // This can only happen if someone messes with the save.
                LogManager.getLogger(API.MOD_ID).warn("Broken save, enum value is invalid.", e);
                return clazz.getEnumConstants()[0];
            }
        } else {
            return clazz.getEnumConstants()[tag.getByte(tagName)];
        }
    }

    public static <T extends Enum<T>> void save(final Enum<T> value, final String tagName, final CompoundTag tag) {
        tag.putByte(tagName, (byte) value.ordinal());
    }

    // --------------------------------------------------------------------- //

    private EnumUtils() {
    }
}
