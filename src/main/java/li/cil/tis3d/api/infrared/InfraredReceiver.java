package li.cil.tis3d.api.infrared;

import li.cil.tis3d.api.machine.Casing;
import net.minecraft.block.Material;
import net.minecraft.util.HitResult;

/**
 * When implemented this will be used let the instance handle a received
 * infrared packet. If the collided with block or entity does not implement
 * this interface, the packet will simply die.
 * <p>
 * Things checked for this interface by default are:
 * <ul>
 * <li>{@link net.minecraft.block.Block}</li>
 * <li>{@link net.minecraft.block.entity.BlockEntity}</li>
 * <li>{@link li.cil.tis3d.api.module.Module} in {@link Casing}.</li>
 * </ul>
 * <p>
 * Note that for non-opaque blocks, defined as <tt>!{@link Material#method_15804()}</tt> this will
 * never be called, as they will be skipped when performing a collision check!
 */
public interface InfraredReceiver {
    /**
     * Called when an infrared packet collides with this.
     *
     * @param packet the packet that collided with this.
     * @param hit    the information on the hit.
     */
    void onInfraredPacket(final InfraredPacket packet, final HitResult hit);
}
