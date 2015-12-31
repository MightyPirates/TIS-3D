package li.cil.tis3d.api.module;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.CasingBase;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A module that can be installed in a TIS-3D {@link Casing}.
 */
public interface Module extends ModuleBase {

    @Override
    Casing getCasing();

    // --------------------------------------------------------------------- //

    /**
     * Called when the module is being installed into a {@link Casing}.
     * <p>
     * This is mainly for convenience and having things in one place, you could
     * just as well restore state in your {@link ModuleProvider}'s
     * {@link ModuleProvider#createModule(ItemStack, Casing, Face)} method.
     * <p>
     * This is called before the first {@link #onEnabled()}, and also <em>before
     * it is actually set in the containing {@link Casing}</em>. Particularly
     * this means {@link Casing#getModule(Face)} for the module's {@link Face}
     * will return <tt>null</tt> in this callback.
     * <p>
     * Note that this is only called on the server.
     *
     * @param stack the item stack the module was created from.
     */
    void onInstalled(ItemStack stack);

    /**
     * Called after the module was uninstalled from a {@link Casing}.
     * <p>
     * This allows storing any data that should be persisted onto the module's
     * item representation. For most modules this will not apply, since they
     * are generally stateless / reset state when the computer shuts down.
     * <p>
     * This is called after the last {@link #onDisabled()} and is equivalent
     * to a <tt>dispose</tt> method (i.e. the module will not be used again
     * after this).
     * <p>
     * Note that this is only called on the server.
     *
     * @param stack the stack representing the module.
     */
    void onUninstalled(ItemStack stack);

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
    boolean onActivate(final EntityPlayer player, final float hitX, final float hitY, final float hitZ);

    /**
     * Called with NBT data sent from the remote instance of the module.
     * <p>
     * This can be called on both the server and the client, depending on which
     * side sent the message (i.e. the client can send messages to the server
     * this way and vice versa).
     *
     * @param nbt the received data.
     * @see Casing#sendData(Face, NBTTagCompound, byte)
     * @see Casing#sendData(Face, NBTTagCompound)
     */
    void onData(final NBTTagCompound nbt);

    /**
     * Called with data sent from the remote instance of the module.
     * <p>
     * This can be called on both the server and the client, depending on which
     * side sent the message (i.e. the client can send messages to the server
     * this way and vice versa).
     *
     * @param data the received data.
     * @see Casing#sendData(Face, ByteBuf, byte)
     * @see Casing#sendData(Face, ByteBuf)
     */
    void onData(ByteBuf data);

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
    void render(final boolean enabled, final float partialTicks);

    // --------------------------------------------------------------------- //

    /**
     * Restore the state of the module from the specified NBT compound.
     *
     * @param nbt the tag to load the state from.
     */
    void readFromNBT(final NBTTagCompound nbt);

    /**
     * Save the state of the module to the specified NBT compound.
     *
     * @param nbt the tag to save the state to.
     */
    void writeToNBT(final NBTTagCompound nbt);
}
