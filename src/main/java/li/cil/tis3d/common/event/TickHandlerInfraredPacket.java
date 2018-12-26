package li.cil.tis3d.common.event;

import li.cil.tis3d.common.entity.InfraredPacketEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Makes sure infrared packets die once their lifetime expires.
 * <p>
 * This would not be guaranteed if they handled this themselves, due to the
 * fact that entities do not update while inside the one-chunk wide border
 * of loaded chunks around the overall area of loaded chunks.
 */
public final class TickHandlerInfraredPacket {
    public static final TickHandlerInfraredPacket INSTANCE = new TickHandlerInfraredPacket();

    // --------------------------------------------------------------------- //

    private final Set<InfraredPacketEntity> livePackets = new HashSet<>();
    private final List<InfraredPacketEntity> pendingRemovals = new ArrayList<>();
    private final List<InfraredPacketEntity> pendingAdds = new ArrayList<>();

    // --------------------------------------------------------------------- //

    public void watchPacket(final InfraredPacketEntity packet) {
        pendingRemovals.remove(packet);
        pendingAdds.add(packet);
    }

    public void unwatchPacket(final InfraredPacketEntity packet) {
        pendingAdds.remove(packet);
        pendingRemovals.add(packet);
    }

    // --------------------------------------------------------------------- //

    public void serverTick() {
        livePackets.addAll(pendingAdds);
        pendingAdds.clear();

        livePackets.removeAll(pendingRemovals);
        pendingRemovals.clear();

        livePackets.forEach(InfraredPacketEntity::updateLifetime);
    }

    // --------------------------------------------------------------------- //

    private TickHandlerInfraredPacket() {
    }
}
