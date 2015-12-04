package li.cil.tis3d.api.module;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Port;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A module that can be installed in a TIS-3D {@link Casing}.
 */
public interface Module {
    /**
     * The {@link Casing} the {@link Module} is installed in.
     *
     * @return the casing the module is installed in.
     */
    Casing getCasing();

    /**
     * The {@link Face} the {@link Module} is installed on in its {@link Casing}.
     *
     * @return the face the module is installed on.
     */
    Face getFace();

    // --------------------------------------------------------------------- //

    /**
     * Advance the state of the module.
     * <p>
     * This is called by the controller of the system the module is part of
     * each tick the system is running.
     */
    default void step() {
    }

    /**
     * Called when the multi-block of casings the module is installed in is
     * enabled, or when the module was installed into an enabled casing.
     * <p>
     * Note that this is only called on the server.
     */
    default void onEnabled() {
    }

    /**
     * Called when the multi-block of casings the module is installed in is
     * disabled, or when the module was removed from an enabled casing.
     * <p>
     * Modules should use this to reset their state, so that cycling power of
     * a controller resets the whole multi-block system.
     * <p>
     * Note that this is only called on the server.
     * <p>
     * When this is called due to a controller or casing being disposed, e.g.
     * due to a chunk being unloaded, the <tt>isDisposing</tt> parameter will
     * be <tt>true</tt>, indicating that the module should avoid world
     * interaction in its clean-up logic (to avoid loading the chunk again).
     */
    default void onDisabled() {
    }

    /**
     * Called from a pipe this module is writing to when the data was read.
     * <p>
     * This allows completing the operation in the same tick in which the
     * read operation was completed. This is particularly useful when writing
     * to multiple ports at a time but the written value may only be read once;
     * in this case the remaining writes can be canceled in this callback.
     *
     * @param port the port on which the write operation was completed.
     */
    default void onWriteComplete(Port port) {
    }

    /**
     * Called when a player right-clicks the module while installed in a casing.
     * <p>
     * The face is implicitly given by the face the module is installed in,
     * as is the world via the casing's world.
     * <p>
     * Note that there should be some way in which a click can be ignored, e.g.
     * by a player sneaking, otherwise the module cannot be removed from the
     * casing by hand.
     *
     * @param player the player that clicked the module.
     * @param hitX   the relative x position that was clicked.
     * @param hitY   the relative y position that was clicked.
     * @param hitZ   the relative z position that was clicked.
     * @return <tt>true</tt> if the click was handled, <tt>false</tt> otherwise.
     */
    default boolean onActivate(EntityPlayer player, float hitX, float hitY, float hitZ) {
        return false;
    }

    /**
     * Called with NBT data sent from the remote instance of the module.
     * <p>
     * This can be called on both the server and the client, depending on which
     * side sent the message (i.e. the client can send messages to the server
     * this way and vice versa).
     *
     * @param nbt the received data.
     * @see {@link Casing#sendData(Face, NBTTagCompound)}
     */
    default void onData(NBTTagCompound nbt) {
    }

    // --------------------------------------------------------------------- //

    /**
     * Called to allow the module to render dynamic content on the casing it
     * is installed in.
     * <p>
     * The render state will be adjusted to take into account the face the
     * module is installed in, i.e. rendering from (0, 0, 0) to (1, 1, 0) will
     * render the full quad of face of the casing the module is installed in.
     *
     * @param enabled      whether the module is currently enabled.
     * @param partialTicks the partial time elapsed in this tick.
     */
    @SideOnly(Side.CLIENT)
    default void render(final boolean enabled, float partialTicks) {
    }

    // --------------------------------------------------------------------- //

    /**
     * Restore the state of the module from the specified NBT compound.
     *
     * @param nbt the tag to load the state from.
     */
    default void readFromNBT(NBTTagCompound nbt) {
    }

    /**
     * Save the state of the module to the specified NBT compound.
     *
     * @param nbt the tag to save the state to.
     */
    default void writeToNBT(NBTTagCompound nbt) {
    }
}
