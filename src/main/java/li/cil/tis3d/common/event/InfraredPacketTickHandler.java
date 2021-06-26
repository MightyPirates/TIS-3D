package li.cil.tis3d.common.event;

import li.cil.tis3d.common.entity.InfraredPacketEntity;
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
public final class InfraredPacketTickHandler {
    private static final Set<InfraredPacketEntity> livePackets = new HashSet<>();
    private static final Set<InfraredPacketEntity> pendingRemovals = new HashSet<>();
    private static final Set<InfraredPacketEntity> pendingAdds = new HashSet<>();

    // --------------------------------------------------------------------- //

    public static void initialize() {
        MinecraftForge.EVENT_BUS.addListener(InfraredPacketTickHandler::onServerTick);
    }

    // --------------------------------------------------------------------- //

    public static void watchPacket(final InfraredPacketEntity packet) {
        pendingRemovals.remove(packet);
        pendingAdds.add(packet);
    }

    public static void unwatchPacket(final InfraredPacketEntity packet) {
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

        livePackets.forEach(InfraredPacketEntity::updateLifetime);
    }
}
