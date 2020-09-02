package li.cil.tis3d.api;

import li.cil.tis3d.api.infrared.InfraredPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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
    InfraredPacket sendPacket(final World world, final Vec3d position, final Vec3d direction, final short value);
}
