package li.cil.tis3d.common.api;

import li.cil.tis3d.api.detail.InfraredAPI;
import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.common.entity.EntityInfraredPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Allow spawning infrared packets externally, reusing our entity.
 */
public final class InfraredAPIImpl implements InfraredAPI {
    @Override
    public InfraredPacket sendPacket(final World world, final Vec3d position, final Vec3d direction, final short value) {
        final EntityInfraredPacket entity = new EntityInfraredPacket(world);
        entity.configure(position, direction.normalize(), value);
        world.spawnEntityInWorld(entity);
        return entity;
    }
}
