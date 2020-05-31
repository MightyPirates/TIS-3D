package li.cil.tis3d.api.module;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

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
    void onInstalled(final ItemStack stack);

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
    void onUninstalled(final ItemStack stack);

    /**
     * Called when the multi-block of casings the module is installed in is
     * enabled, or when the module was installed into an enabled casing.
     * <p>
     * Note that this is only called on the server.
     */
    void onEnabled();

    /**
     * Called when the multi-block of casings the module is installed in is
     * disabled, or when the module was removed from an enabled casing.
     * <p>
     * Modules should use this to reset their state, so that cycling power of
     * a controller resets the whole multi-block system.
     * <p>
     * Note that this is only called on the server.
     */
    void onDisabled();

    /**
     * Called when the {@link Casing} housing the module is being disposed,
     * e.g. due to a chunk being unloaded.
     * <p>
     * This is intended for freeing up resources (e.g. allocated texture or
     * audio memory). Unlike {@link #onDisabled()} this is only called once
     * on a module, at the very end of its life. Avoid world interaction in
     * this callback to avoid loading the chunk again.
     * <p>
     * This is called on the server <em>and</em> the client.
     */
    void onDisposed();

    /**
     * Called from a pipe this module is writing to when the data is being read.
     * <p>
     * The key difference to {@link #onWriteComplete(Port)} is that this is called
     * directly during the read operation, so this may be called before or after
     * the module's {@link #step()} for the current cycle. As such, no operations
     * which might influence number of required cycles for the current transfer
     * operation should be performed in this method. It may be used to cancel
     * other write operations to write a value only once, for example, or to update
     * some other internal state before a read operation completes (used by the
     * stack module for example, to ensure the correct value is being popped).
     *
     * @param port the port on which the write operation will be completed.
     */
    void onBeforeWriteComplete(final Port port);

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
    void onWriteComplete(final Port port);

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
     * @param hand   the hand the player used to activate the module.
     * @param hit    the relative hit position that was clicked.
     * @return <tt>true</tt> if the click was handled, <tt>false</tt> otherwise.
     */
    boolean onActivate(final PlayerEntity player, final Hand hand, final Vec3d hit);

    /**
     * Called with NBT data sent from the remote instance of the module.
     * <p>
     * This can be called on both the server and the client, depending on which
     * side sent the message (i.e. the client can send messages to the server
     * this way and vice versa).
     *
     * @param nbt the received data.
     * @see Casing#sendData(Face, CompoundTag, byte)
     * @see Casing#sendData(Face, CompoundTag)
     */
    void onData(final CompoundTag nbt);

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
    void onData(final ByteBuf data);

    // --------------------------------------------------------------------- //

    /**
     * Called to allow the module to render dynamic content on the casing it
     * is installed in.
     * <p>
     * The render state will be adjusted to take into account the face the
     * module is installed in, i.e. rendering from (0, 0, 0) to (1, 1, 0) will
     * render the full quad of face of the casing the module is installed in.
     * <p>
     * Note that the <code>enabled</code> is the same as {@link Casing#isEnabled()},
     * it is merely passed along for backwards compatibility from before the
     * time that getter existed.
     *
     * @param rendererDispatcher the render context of the tile entity the module sits in.
     * @param partialTicks       the partial time elapsed in this tick.
     */
    void render(final BlockEntityRenderDispatcher rendererDispatcher, final float partialTicks);

    void render(final BlockEntityRenderDispatcher rendererDispatcher, final float partialTicks,
                final MatrixStack matrices, final VertexConsumerProvider vcp,
                final int light, final int overlay);

    // --------------------------------------------------------------------- //

    /**
     * Restore the state of the module from the specified NBT compound.
     *
     * @param nbt the tag to load the state from.
     */
    void readFromNBT(final CompoundTag nbt);

    /**
     * Save the state of the module to the specified NBT compound.
     *
     * @param nbt the tag to save the state to.
     */
    void writeToNBT(final CompoundTag nbt);
}
