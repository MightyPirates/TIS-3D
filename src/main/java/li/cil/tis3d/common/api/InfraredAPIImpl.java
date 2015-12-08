package li.cil.tis3d.common.api;

import li.cil.tis3d.api.detail.InfraredAPI;
import li.cil.tis3d.common.entity.EntityInfraredPacket;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Allow spawning infrared packets externally, reusing our entity.
 */
public final class InfraredAPIImpl implements InfraredAPI {
    @Override
    public void sendPacket(final World world, final Vec3 position, final Vec3 direction, final int value) {
        final EntityInfraredPacket entity = new EntityInfraredPacket(world);
        entity.configure(position, direction.normalize(), value);
        world.spawnEntityInWorld(entity);
    }
}
