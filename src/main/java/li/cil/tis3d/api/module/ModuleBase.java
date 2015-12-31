package li.cil.tis3d.api.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.CasingBase;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;

/**
 * The common functions between a full Module and a LightweightModule.
 * Cannot, by itself, be used for anything
 */
public interface ModuleBase {
    /**
     * The {@link Casing} the {@link Module} is installed in.
     *
     * @return the casing the module is installed in.
     */
    CasingBase getCasing();

    /**
     * The {@link Face} the {@link Module} is installed on in its {@link Casing}.
     *
     * @return the face the module is installed on.
     */
    Face getFace();

    /**
     * Advance the state of the module.
     * <p>
     * This is called by the controller of the system the module is part of
     * each tick the system is running.
     */
    void step();

    /**
     * Called when the multi-block of casings the module is installed in is
     * enabled, or when the module was installed into an enabled casing.
     * <p>
     * Note that this is only called on the server.
     */
    void onEnabled();

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
}
