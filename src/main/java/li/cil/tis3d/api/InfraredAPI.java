package li.cil.tis3d.api;

import li.cil.tis3d.api.infrared.InfraredPacket;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * API entry point for spawning infrared packets and related tasks.
 * <p>
 * This is made available in the init phase, so you'll either have to (soft)
 * depend on TIS-3D or you must not make calls to this before the init phase.
 */
public final class InfraredAPI {
    /**
     * Emit a new infrared packet with the specified value.
     *
     * @param world     the world to spawn the packet in.
     * @param position  the location to spawn the packet at.
     * @param direction the direction the packet shall travel in.
     * @param value     the value the packet carries.
     * @return the packet that was spawned.
     */
    public static InfraredPacket sendPacket(final World world, final Vec3 position, final Vec3 direction, final short value) {
        if (API.infraredAPI != null) {
            return API.infraredAPI.sendPacket(world, position, direction, value);
        }
        return null;
    }

    // --------------------------------------------------------------------- //

    private InfraredAPI() {
    }
}
