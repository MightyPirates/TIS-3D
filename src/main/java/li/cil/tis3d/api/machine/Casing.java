package li.cil.tis3d.api.machine;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.module.Module;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * A casing for TIS-3D modules.
 * <p>
 * This is implemented by the tile entity of TIS-3D casings.
 */
public interface Casing extends CasingBase {

    /**
     * The world this casing resides in.
     *
     * @return the world the casing lives in.
     */
    World getCasingWorld();

    /**
     * The position of the casing in the world it exists in.
     *
     * @return the position of the casing.
     */
    BlockPos getPosition();

    // --------------------------------------------------------------------- //

    /**
     * Get whether the casing is locked.
     * <p>
     * Casings can be locked, preventing players to remove modules from the
     * casing or add modules to the casing. Some modules may choose to also
     * ignore {@link Module#onActivate(EntityPlayer, float, float, float)}
     * calls while their casing is locks (such as the execution module to
     * prevent reprogramming).
     *
     * @return <tt>true</tt> if the casing is currently locked.
     */
    boolean isLocked();

    @Override
    Module getModule(Face face);

    // --------------------------------------------------------------------- //

    /**
     * Call this to send some data from a module to it's other representation.
     * <p>
     * That is, when called from the client, it will send the data to the
     * instance representing the module on the specified face on the server,
     * when called on the server it will send the data to the client.
     * <p>
     * Data is collected each tick, and sent in one big packet. If more than
     * one send request is performed in one tick with the same type, the data
     * will replace the previously queued data. A negative value indicates
     * that no specific type is set and data should not be replaced in the
     * send queue.
     * <p>
     * Note that this is a convenience alternative for {@link #sendData(Face, ByteBuf, byte)}
     * that is meant to be used for <em>non-frequent</em> data, i.e. data
     * that's only sent every so often. For data you expect may be sent
     * each tick, prefer using the more light-weight {@link ByteBuf}.
     *
     * @param face the face the module is installed in.
     * @param data the data to send to the client.
     * @param type the type of the data being sent.
     */
    void sendData(Face face, NBTTagCompound data, final byte type);

    /**
     * Call this to send some data from a module to it's other representation.
     * <p>
     * This behaves like {@link #sendData(Face, ByteBuf, byte)}, except
     * with no specific type associated, so new data will never replace old
     * data. Where at all possible, providing a type is strongly recommended,
     * to reduce generated network traffic.
     * <p>
     * Note that this is a convenience alternative for {@link #sendData(Face, ByteBuf)}
     * that is meant to be used for <em>non-frequent</em> data, i.e. data
     * that's only sent every so often. For data you expect may be sent
     * each tick, prefer using the more light-weight {@link ByteBuf}.
     *
     * @param face the face the module is installed in.
     * @param data the data to send to the client.
     */
    void sendData(Face face, NBTTagCompound data);

    /**
     * Call this to send some data from a module to it's other representation.
     * <p>
     * That is, when called from the client, it will send the data to the
     * instance representing the module on the specified face on the server,
     * when called on the server it will send the data to the client.
     * <p>
     * Data is collected each tick, and sent in one big packet. If more than
     * one send request is performed in one tick with the same type, the data
     * will replace the previously queued data. A negative value indicates
     * that no specific type is set and data should not be replaced in the
     * send queue.
     *
     * @param face the face the module is installed in.
     * @param data the data to send to the client.
     * @param type the type of the data being sent.
     */
    void sendData(Face face, ByteBuf data, final byte type);

    /**
     * Call this to send some data from a module to it's other representation.
     * <p>
     * This behaves like {@link #sendData(Face, ByteBuf, byte)}, except
     * with no specific type associated, so new data will never replace old
     * data. Where at all possible, providing a type is strongly recommended,
     * to reduce generated network traffic.
     *
     * @param face the face the module is installed in.
     * @param data the data to send to the client.
     */
    void sendData(Face face, ByteBuf data);

    /**
     * Send an ordered packet.
     * This will receive the data in the order it was sent.
     * Meant for cases like delta-encoding, where the packet-reduction techniques cause more problems than they're worth.
     *
     * @param face the face the module is installed in
     * @param data the data to send to the client
     */
    void sendOrderedData(Face face, ByteBuf data);

    /**
     * Send an ordered packet.
     * This will receive the data in the order it was sent.
     * Meant for cases like delta-encoding, where the packet-reduction techniques cause more problems than they're worth.
     *
     * @param face the face the module is installed in
     * @param data the data to send to the client
     */
    void sendOrderedData(Face face, ByteBuf data, final byte type);
}
