package li.cil.tis3d.common.event;

import li.cil.tis3d.common.entity.EntityInfraredPacket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Makes sure infrared packets die once their lifetime expires.
 * <p>
 * This would not be guaranteed if they handled this themselves, due to the
 * fact that entities do not update while inside the one-chunk wide border
 * of loaded chunks around the overall area of loaded chunks.
 */
public final class TickHandlerInfraredPacket {
    private static final Set<EntityInfraredPacket> livePackets = new HashSet<>();
    private static final Set<EntityInfraredPacket> pendingRemovals = new HashSet<>();
    private static final Set<EntityInfraredPacket> pendingAdds = new HashSet<>();

    // --------------------------------------------------------------------- //

    public static void initialize() {
        MinecraftForge.EVENT_BUS.addListener(TickHandlerInfraredPacket::onServerTick);
    }

    // --------------------------------------------------------------------- //

    public static void watchPacket(final EntityInfraredPacket packet) {
        pendingRemovals.remove(packet);
        pendingAdds.add(packet);
    }

    public static void unwatchPacket(final EntityInfraredPacket packet) {
        pendingAdds.remove(packet);
        pendingRemovals.add(packet);
    }

    // --------------------------------------------------------------------- //

    private static void onServerTick(final TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        livePackets.addAll(pendingAdds);
        pendingAdds.clear();

        livePackets.removeAll(pendingRemovals);
        pendingRemovals.clear();

        livePackets.forEach(EntityInfraredPacket::updateLifetime);
    }
}
