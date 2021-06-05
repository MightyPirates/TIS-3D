package li.cil.tis3d.api.machine;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.module.Module;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * A casing for TIS-3D modules.
 * <p>
 * This is implemented by the tile entity of TIS-3D casings.
 */
public interface Casing {
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

    /**
     * Flag the casing as dirty so it is saved when the chunk containing it
     * saved next.
     */
    void markDirty();

    // --------------------------------------------------------------------- //

    /**
     * Get whether the casing is currently enabled, i.e. whether it is
     * connected to a currently enabled controller.
     * <p>
     * A controller is considered active when a positive non-zero redstone
     * signal is applied to it. This in particular includes the paused state
     * at a signal strength one.
     * <p>
     * This is useful for contextual behavior in modules while rendering or in
     * the activation callback {@link Module#onActivate(PlayerEntity, Hand, Vec3d)}.
     *
     * @return whether the casing is currently enabled.
     */
    boolean isEnabled();

    /**
     * Get whether the casing is locked.
     * <p>
     * Casings can be locked, preventing players to remove modules from the
     * casing or add modules to the casing. Some modules may choose to also
     * ignore {@link Module#onActivate(PlayerEntity, Hand, Vec3d)}
     * calls while their casing is locks (such as the execution module to
     * prevent reprogramming).
     *
     * @return <tt>true</tt> if the casing is currently locked.
     */
    boolean isLocked();

    /**
     * Get the module installed on the specified face of the casing.
     *
     * @param face the face to get the module for.
     * @return the module installed on that face, or <tt>null</tt>.
     */
    @Nullable
    Module getModule(final Face face);

    /**
     * Get the receiving pipe on the specified port of a module in this casing.
     * <p>
     * There are two {@link Pipe}s between every pair of {@link Module}s
     * in a case. Specifically, each edge of a {@link Casing} has two
     * {@link Pipe}s, going into opposite directions. This method is used
     * to to get a {@link Pipe} based on its sink.
     *
     * @param face the face to get the port for.
     * @param port the port for which to get the port.
     * @return the input port on that port.
     */
    Pipe getReceivingPipe(final Face face, final Port port);

    /**
     * Get the sending pipe on the specified port of a module in this casing.
     * <p>
     * There are two {@link Pipe}s between every pair of {@link Module}s
     * in a case. Specifically, each edge of a {@link Casing} has two
     * {@link Pipe}s, going into opposite directions. This method is used
     * to to get a {@link Pipe} based on its source.
     *
     * @param face the face to get the port for.
     * @param port the port for which to get the port.
     * @return the output port on that port.
     */
    Pipe getSendingPipe(final Face face, final Port port);

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
     * <p>
     * <em>Important</em>: the passed NBT tag is <em>not</em> copied, it is
     * stored by reference. If you intend to modify the tag after passing
     * it to this method, pass a copy of it instead.
     *
     * @param face the face the module is installed in.
     * @param data the data to send to the client.
     * @param type the type of the data being sent.
     */
    void sendData(final Face face, final NbtCompound data, final byte type);

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
     * <p>
     * <em>Important</em>: the passed NBT tag is <em>not</em> copied, it is
     * stored by reference. If you intend to modify the tag after passing
     * it to this method, pass a copy of it instead.
     *
     * @param face the face the module is installed in.
     * @param data the data to send to the client.
     */
    void sendData(final Face face, final NbtCompound data);

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
     * <em>Important</em>: the passed buffer is <em>not</em> copied, it is
     * stored by reference. If you intend to modify the buffer after passing
     * it to this method, pass a copy of it instead.
     *
     * @param face the face the module is installed in.
     * @param data the data to send to the client.
     * @param type the type of the data being sent.
     */
    void sendData(final Face face, final ByteBuf data, final byte type);

    /**
     * Call this to send some data from a module to it's other representation.
     * <p>
     * This behaves like {@link #sendData(Face, ByteBuf, byte)}, except
     * with no specific type associated, so new data will never replace old
     * data. Where at all possible, providing a type is strongly recommended,
     * to reduce generated network traffic.
     * <p>
     * <em>Important</em>: the passed buffer is <em>not</em> copied, it is
     * stored by reference. If you intend to modify the buffer after passing
     * it to this method, pass a copy of it instead.
     *
     * @param face the face the module is installed in.
     * @param data the data to send to the client.
     */
    void sendData(final Face face, final ByteBuf data);
}
