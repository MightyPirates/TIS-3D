package li.cil.tis3d.api.detail;

import li.cil.tis3d.api.infrared.InfraredPacket;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * API entry point for spawning {@link InfraredPacket}s and related tasks.
 */
public interface InfraredAPI {
    /**
     * Emit a new infrared packet with the specified value.
     *
     * @param world     the world to spawn the packet in.
     * @param position  the location to spawn the packet at.
     * @param direction the direction the packet shall travel in.
     * @param value     the value the packet carries.
     * @return the packet that was spawned.
     */
    @Nullable
    InfraredPacket sendPacket(final World world, final Vector3d position, final Vector3d direction, final short value);
}
