package li.cil.tis3d.api.module;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

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
    void step();

    /**
     * Called from a port this module is writing to when the data was read.
     * <p>
     * This allows completing the operation in the same tick in which the
     * read operation was completed. This is particularly useful when writing
     * to multiple ports at a time but the written value may only be read once;
     * in this case the remaining writes can be canceled in this callback.
     */
    void onWriteComplete(Side side);

    // --------------------------------------------------------------------- //

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
    boolean onActivate(EntityPlayer player, float hitX, float hitY, float hitZ);

    // --------------------------------------------------------------------- //

    /**
     * Restore the state of the module from the specified NBT compound.
     *
     * @param nbt the tag to load the state from.
     */
    void readFromNBT(NBTTagCompound nbt);

    /**
     * Save the state of the module to the specified NBT compound.
     *
     * @param nbt the tag to save the state to.
     */
    void writeToNBT(NBTTagCompound nbt);
}
