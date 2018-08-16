package li.cil.tis3d.api.infrared;

import li.cil.tis3d.api.machine.Casing;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.RayTraceResult;

/**
 * When implemented this will be used let the instance handle a received
 * infrared packet. If the collided with block or packet does not implement
 * this interface, the packet will simply die.
 * <p>
 * Things checked for this interface are:
 * <ul>
 * <li>{@link net.minecraft.block.Block}</li>
 * <li>{@link li.cil.tis3d.api.module.Module} in {@link Casing}.</li>
 * </ul>
 * <p>
 * Things checked for capabilities of this type are:
 * <ul>
 * <li>{@link net.minecraft.entity.Entity}</li>
 * <li>{@link net.minecraft.tileentity.TileEntity}</li>
 * </ul>
 * <p>
 * For compatibility, entities and tile entities implementing this interface will have
 * the corresponding capability attached automatically.
 * <p>
 * Note that for non-opaque blocks, defined as <tt>!{@link Material#blocksMovement()} ||
 * !{@link Material#isOpaque()}</tt> this will
 * never be called, as they will be skipped when performing a collision check!
 */
public interface InfraredReceiver {
    /**
     * Called when an infrared packet collides with this.
     *
     * @param packet the packet that collided with this.
     * @param hit    the information on the hit.
     */
    void onInfraredPacket(final InfraredPacket packet, final RayTraceResult hit);
}
