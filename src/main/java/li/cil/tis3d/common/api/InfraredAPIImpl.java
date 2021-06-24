package li.cil.tis3d.common.api;

import li.cil.tis3d.api.detail.InfraredAPI;
import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.common.entity.Entities;
import li.cil.tis3d.common.entity.EntityInfraredPacket;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/**
 * Allow spawning infrared packets externally, reusing our entity.
 */
public final class InfraredAPIImpl implements InfraredAPI {
    @Override
    public InfraredPacket sendPacket(final World world, final Vector3d position, final Vector3d direction, final short value) {
        final EntityInfraredPacket entity = Entities.INFRARED_PACKET.get().create(world);
        if (entity != null) {
            entity.configure(position, direction.normalize(), value);
            world.addFreshEntity(entity);
        }
        return entity;
    }
}
