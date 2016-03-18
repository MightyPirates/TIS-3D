package li.cil.tis3d.api.prefab.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.traits.Rotatable;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This is a utility implementation of a rotatable module.
 * <p>
 * Rotatable modules can face one of four directions, the default being
 * {@link Port#UP}. Most modules will either not need
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
    // Rendering utility

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
    // General utility

    @Override
    protected Vec3d hitToUV(final Vec3d hitPos) {
        final Vec3d uv = super.hitToUV(hitPos);
        switch (getFace()) {
            case Y_NEG:
                switch (getFacing()) {
                    case LEFT:
                        return new Vec3d(uv.yCoord, 1 - uv.xCoord, 0);
                    case RIGHT:
                        return new Vec3d(1 - uv.yCoord, uv.xCoord, 0);
                    case UP:
                        return uv;
                    case DOWN:
                        return new Vec3d(1 - uv.xCoord, 1 - uv.yCoord, 0);
                }
                break;
            case Y_POS:
                switch (getFacing()) {
                    case LEFT:
                        return new Vec3d(1 - uv.yCoord, uv.xCoord, 0);
                    case RIGHT:
                        return new Vec3d(uv.yCoord, 1 - uv.xCoord, 0);
                    case UP:
                        return uv;
                    case DOWN:
                        return new Vec3d(1 - uv.xCoord, 1 - uv.yCoord, 0);
                }
                break;
        }
        return uv;
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        facing = EnumUtils.readFromNBT(Port.class, TAG_FACING, nbt);
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        EnumUtils.writeToNBT(facing, TAG_FACING, nbt);
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
