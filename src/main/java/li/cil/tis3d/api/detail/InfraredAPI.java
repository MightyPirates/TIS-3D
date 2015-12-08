package li.cil.tis3d.api.detail;

import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * API entry point for spawning {@link li.cil.tis3d.api.infrared.InfraredPacket}s and related tasks.
 */
public interface InfraredAPI {
    /**
     * Emit a new infrared packet with the specified value.
     *
     * @param world     the world to spawn the packet in.
     * @param position  the location to spawn the packet at.
     * @param direction the direction the packet shall travel in.
     * @param value     the value the packet carries.
     */
    void sendPacket(final World word, final Vec3 position, final Vec3 direction, final int value);
}
