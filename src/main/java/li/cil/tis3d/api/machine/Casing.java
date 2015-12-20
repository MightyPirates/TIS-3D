package li.cil.tis3d.api.machine;

import li.cil.tis3d.api.module.Module;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

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
     * The x position of the casing in the world it exists in.
     *
     * @return the x position of the casing.
     */
    int getPositionX();

    /**
     * The y position of the casing in the world it exists in.
     *
     * @return the y position of the casing.
     */
    int getPositionY();

    /**
     * The z position of the casing in the world it exists in.
     *
     * @return the z position of the casing.
     */
    int getPositionZ();

    /**
     * Flag the casing as dirty so it is saved when the chunk containing it
     * saved next.
     */
    void markDirty();

    // --------------------------------------------------------------------- //

    /**
     * Get the module installed on the specified face of the casing.
     *
     * @param face the face to get the module for.
     * @return the module installed on that face, or <tt>null</tt>.
     */
    Module getModule(Face face);

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
    Pipe getReceivingPipe(Face face, Port port);

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
    Pipe getSendingPipe(Face face, Port port);

    // --------------------------------------------------------------------- //

    /**
     * Call this to send some data from a module to it's other representation.
     * <p>
     * That is, when called from the client, it will send the data to the
     * instance representing the module on the specified face on the server,
     * when called on the server it will send the data to the client.
     *
     * @param face the face the module is installed in.
     * @param data the data to send to the client.
     */
    void sendData(Face face, NBTTagCompound data);
}
