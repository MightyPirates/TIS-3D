package li.cil.tis3d.common.api;

import li.cil.tis3d.api.detail.InfraredAPI;
import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.common.entity.Entities;
import li.cil.tis3d.common.entity.InfraredPacketEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Allow spawning infrared packets externally, reusing our entity.
 */
public final class InfraredAPIImpl implements InfraredAPI {
    @Override
    public InfraredPacket sendPacket(final Level level, final Vec3 position, final Vec3 direction, final short value) {
        final InfraredPacketEntity entity = Entities.INFRARED_PACKET.get().create(level);
        if (entity != null) {
            entity.configure(position, direction.normalize(), value);
            level.addFreshEntity(entity);
        }
        return entity;
    }
}
