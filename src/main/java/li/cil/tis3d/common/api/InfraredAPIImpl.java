package li.cil.tis3d.common.api;

import li.cil.tis3d.api.detail.InfraredAPI;
import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.common.entity.InfraredPacketEntity;
import li.cil.tis3d.common.init.Entities;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Allow spawning infrared packets externally, reusing our entity.
 */
public final class InfraredAPIImpl implements InfraredAPI {
    @Override
    public InfraredPacket sendPacket(final World world, final Vec3d position, final Vec3d direction, final short value) {
        final InfraredPacketEntity entity = Entities.INFRARED_PACKET.create(world);
        if (entity != null) {
            entity.configure(position, direction.normalize(), value);
            world.spawnEntity(entity);
        }
        return entity;
    }
}
