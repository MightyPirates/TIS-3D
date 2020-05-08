package li.cil.tis3d.api.prefab.module;

import com.mojang.blaze3d.platform.GlStateManager;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.traits.Rotatable;
import li.cil.tis3d.api.util.TransformUtil;
import li.cil.tis3d.util.EnumUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

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
public abstract class AbstractModuleWithRotation extends AbstractModule implements Rotatable {
    // --------------------------------------------------------------------- //
    // Persisted data

    private Port facing = Port.UP;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT tag names.
    private static final String TAG_FACING = "facing";

    // --------------------------------------------------------------------- //

    protected AbstractModuleWithRotation(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Rendering utility

    /**
     * Apply the module's rotation to the OpenGL state.
     */
    @Environment(EnvType.CLIENT)
    protected void rotateForRendering() {
        final int rotation = Port.ROTATION[getFacing().ordinal()];
        GlStateManager.translatef(0.5f, 0.5f, 0);
        GlStateManager.rotatef(90 * rotation, 0, 0, Face.toDirection(getFace()).getOffsetY());
        GlStateManager.translatef(-0.5f, -0.5f, 0);
    }

    @Environment(EnvType.CLIENT)
    protected void rotateForRendering(final MatrixStack matrices) {
        final int rotDeg = 90 * Port.ROTATION[getFacing().ordinal()];
        final Vector3f rotAxis = new Vector3f(0, 0, Face.toDirection(getFace()).getOffsetY());
        final Quaternion rotQ = new Quaternion(rotAxis, rotDeg, true);

        matrices.translate(0.5f, 0.5f, 0);
        matrices.multiply(rotQ);
        matrices.translate(-0.5f, -0.5f, 0);
    }

    // --------------------------------------------------------------------- //
    // General utility

    @Override
    protected Vec3d hitToUV(final Vec3d hitPos) {
        return TransformUtil.hitToUV(getFace(), getFacing(), hitPos);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void readFromNBT(final CompoundTag nbt) {
        super.readFromNBT(nbt);
        facing = EnumUtils.readFromNBT(Port.class, TAG_FACING, nbt);
    }

    @Override
    public void writeToNBT(final CompoundTag nbt) {
        super.writeToNBT(nbt);
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
