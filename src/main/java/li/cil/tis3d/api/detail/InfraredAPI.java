package li.cil.tis3d.api.detail;

import li.cil.tis3d.api.infrared.InfraredPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * API entry point for spawning {@link InfraredPacket}s and related tasks.
 */
public interface InfraredAPI {
    /**
     * Emit a new infrared packet with the specified value.
     *
     * @param level     the level to spawn the packet in.
     * @param position  the location to spawn the packet at.
     * @param direction the direction the packet shall travel in.
     * @param value     the value the packet carries.
     * @return the packet that was spawned.
     */
    @Nullable
    InfraredPacket sendPacket(final Level level, final Vec3 position, final Vec3 direction, final short value);
}
