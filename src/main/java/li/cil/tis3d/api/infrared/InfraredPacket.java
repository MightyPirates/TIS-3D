package li.cil.tis3d.api.infrared;

import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

/**
 * Represents an infrared packet carrying a single value.
 * <p>
 * Use the {@link InfraredReceiver} interface to interact with these.
 */
public interface InfraredPacket {
    /**
     * Get the value carried by this packet.
     *
     * @return the value carried by the packet.
     */
    short getPacketValue();

    /**
     * Get the current position of the packet.
     *
     * @return the current position of the packet.
     */
    Vec3 getPacketPosition();

    /**
     * Get the normalized direction the packet is currently heading.
     *
     * @return the current heading of the packet.
     */
    Vec3 getPacketDirection();

    /**
     * Instead of consuming the packet, this can be used to redirect the
     * packet into a new direction. Use this to implement relays like
     * mirrors if you like. You can also use this to increase (or decrease)
     * a packet's lifetime.
     * <p>
     * When changing the direction of a packet, you may want to set the new
     * position to the hit position (passed to {@link InfraredReceiver#onInfraredPacket(InfraredPacket, MovingObjectPosition)}).
     * Any changes to the position will be taken into account automatically,
     * so that packets do not travel further due to this than they otherwise
     * would. Note that you cannot move a packet further than it would travel
     * in one tick anyway (4 blocks).
     *
     * @param position      the new current position of the packet.
     * @param direction     the new direction the packet should be heading.
     * @param addedLifetime how many ticks to add to the packet's lifetime.
     */
    void redirectPacket(Vec3 position, Vec3 direction, int addedLifetime);
}
