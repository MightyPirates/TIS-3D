package li.cil.tis3d.util.datafix.fixes;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.Constants;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.Objects;

public final class TileEntityLeadingWhitespace implements IFixableData {
    private static final String TAG_ID = "id";
    private static final String LEADING_WHITESPACE_CASING_ID = API.MOD_ID + ": " + Constants.NAME_BLOCK_CASING;
    private static final String CASING_ID = API.MOD_ID + ':' + Constants.NAME_BLOCK_CASING;
    private static final String LEADING_WHITESPACE_CONTROLLER_ID = API.MOD_ID + ": " + Constants.NAME_BLOCK_CONTROLLER;
    private static final String CONTROLLER_ID = API.MOD_ID + ':' + Constants.NAME_BLOCK_CONTROLLER;

    @Override
    public int getFixVersion() {
        return 1;
    }

    @Override
    public NBTTagCompound fixTagCompound(final NBTTagCompound compound) {
        if (compound.hasKey(TAG_ID, NBT.TAG_STRING)) {
            final String id = compound.getString(TAG_ID);
            if (Objects.equals(id, LEADING_WHITESPACE_CASING_ID)) {
                compound.setString(TAG_ID, CASING_ID);
            } else if (Objects.equals(id, LEADING_WHITESPACE_CONTROLLER_ID)) {
                compound.setString(TAG_ID, CONTROLLER_ID);
            }
        }
        return compound;
    }
}
