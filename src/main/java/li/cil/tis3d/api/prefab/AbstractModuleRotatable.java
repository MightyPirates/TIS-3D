package li.cil.tis3d.api.prefab;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.api.module.Rotatable;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;

/**
 * This is a utility implementation of a rotatable module.
 * <p>
 * Rotatable modules can face one of four directions, the default being
 * {@link li.cil.tis3d.api.Port#UP}. Most modules will either not need
 * this at all, or only use this when installed in the top or bottom faces
 * of casings. In some cases you may also merely want to use this for
 * graphical purposes (e.g. the built-in redstone and stack modules do
 * this).
 */
public abstract class AbstractModuleRotatable extends AbstractModule implements Rotatable {
    // --------------------------------------------------------------------- //
    // Persisted data

    private Port facing = Port.UP;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT tag names.
    private static final String TAG_FACING = "facing";

    // --------------------------------------------------------------------- //

    protected AbstractModuleRotatable(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //

    /**
     * Apply the module's rotation to the OpenGL state.
     */
    @SideOnly(Side.CLIENT)
    protected void rotateForRendering() {
        final int rotation = Port.ROTATION[facing.ordinal()];
        GlStateManager.translate(0.5f, 0.5f, 0);
        GlStateManager.rotate(90 * rotation, 0, 0, Face.toEnumFacing(getFace()).getFrontOffsetY());
        GlStateManager.translate(-0.5f, -0.5f, 0);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        if (nbt.hasKey(TAG_FACING)) {
            try {
                facing = Enum.valueOf(Port.class, nbt.getString(TAG_FACING));
            } catch (final IllegalArgumentException e) {
                // This can only happen if someone messes with the save.
                LogManager.getLogger(API.MOD_ID).warn("Broken save, module facing is invalid.", e);
            }
        }
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        nbt.setString(TAG_FACING, facing.name());
    }

    // --------------------------------------------------------------------- //
    // Rotatable

    @Override
    public Port getFacing() {
        return facing;
    }

    @Override
    public void setFacing(final Port facing) {
        this.facing = facing;
    }
}
