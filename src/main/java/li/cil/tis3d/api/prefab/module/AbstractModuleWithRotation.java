package li.cil.tis3d.api.prefab.module;

import com.mojang.blaze3d.matrix.MatrixStack;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.traits.ModuleWithRotation;
import li.cil.tis3d.api.util.TransformUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
public abstract class AbstractModuleWithRotation extends AbstractModule implements ModuleWithRotation {
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
     *
     * @param matrixStack the current matrix stack.
     */
    @OnlyIn(Dist.CLIENT)
    protected void rotateForRendering(final MatrixStack matrixStack) {
        final int rotation = Port.ROTATION[getFacing().ordinal()];
        matrixStack.translate(0.5f, 0.5f, 0);
        matrixStack.mulPose(new Quaternion(0, 0, 90 * rotation * Face.toDirection(getFace()).getStepY(), true));
        matrixStack.translate(-0.5f, -0.5f, 0);
    }

    // --------------------------------------------------------------------- //
    // General utility

    @Override
    protected Vector3d hitToUV(final Vector3d hitPos) {
        return TransformUtil.hitToUV(getFace(), getFacing(), hitPos);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void readFromNBT(final CompoundNBT nbt) {
        super.readFromNBT(nbt);

        facing = Port.VALUES[Math.max(0, nbt.getByte(TAG_FACING)) % Port.VALUES.length];
    }

    @Override
    public void writeToNBT(final CompoundNBT nbt) {
        super.writeToNBT(nbt);

        nbt.putByte(TAG_FACING, (byte) facing.ordinal());
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
