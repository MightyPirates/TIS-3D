package li.cil.tis3d.common.event;

import li.cil.tis3d.common.entity.EntityInfraredPacket;
import net.minecraft.server.MinecraftServer;
import org.dimdev.rift.listener.ServerTickable;

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
public final class TickHandlerInfraredPacket implements ServerTickable {
    public static final TickHandlerInfraredPacket INSTANCE = new TickHandlerInfraredPacket();

    // --------------------------------------------------------------------- //

    private final Set<EntityInfraredPacket> livePackets = new HashSet<>();
    private final List<EntityInfraredPacket> pendingRemovals = new ArrayList<>();
    private final List<EntityInfraredPacket> pendingAdds = new ArrayList<>();

    // --------------------------------------------------------------------- //

    public void watchPacket(final EntityInfraredPacket packet) {
        pendingRemovals.remove(packet);
        pendingAdds.add(packet);
    }

    public void unwatchPacket(final EntityInfraredPacket packet) {
        pendingAdds.remove(packet);
        pendingRemovals.add(packet);
    }

    // --------------------------------------------------------------------- //

 /*   @SubscribeEvent
    public void onServerTick(final TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        livePackets.addAll(pendingAdds);
        pendingAdds.clear();

        livePackets.removeAll(pendingRemovals);
        pendingRemovals.clear();

        livePackets.forEach(EntityInfraredPacket::updateLifetime);
    } */

    // --------------------------------------------------------------------- //

    private TickHandlerInfraredPacket() {
    }

    @Override
    public void serverTick(MinecraftServer server) {
        livePackets.addAll(pendingAdds);
        pendingAdds.clear();

        livePackets.removeAll(pendingRemovals);
        pendingRemovals.clear();

        livePackets.forEach(EntityInfraredPacket::updateLifetime);
    }
}
