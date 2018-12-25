package li.cil.tis3d.api;

import li.cil.tis3d.api.module.traits.BundledRedstoneOutputChangedEvent;

import java.util.ArrayList;

/**
 * API entry point for TIS-3D specific bundled redstone events.
 */
public final class BundledRedstoneAPI {
    /**
     * Register an event listener for bundled redstone output changes on modules.
     *
     * @param listener the listener to register.
     */
    public static void addListener(final BundledRedstoneOutputChangedEvent.Listener listener) {
        listeners.add(listener);
    }

    /**
     * Unregister an event listener for bundled redstone output changes on modules.
     *
     * @param listener the listener to unregister.
     */
    public static void removeListener(final BundledRedstoneOutputChangedEvent.Listener listener) {
        listeners.remove(listener);
    }

    /**
     * Dispatch a bundled redstone output change event for a module.
     * <p>
     * Depending on the version/context/environment this mod is used in, this event may, in addtion to
     * notifying listeners added via {@link #addListener(BundledRedstoneOutputChangedEvent.Listener)},
     * <em>additionally</em> be fired on a mod framework's event bus (e.g. <c>MinecraftForge.EVENT_BUS</c>).
     *
     * @param event the event to dispatch, holding the module having changed its output and the changed channel.
     */
    public static void onBundledRedstoneOutputChanged(final BundledRedstoneOutputChangedEvent event) {
        listeners.forEach(listener -> listener.onBundledRedstoneOutputChanged(event));

        // TODO Also dispatch on modding framework specific event bus, e.g. MinecraftForge.EVENT_BUS, RiftLoader.instance.getListeners, ...
    }

    // --------------------------------------------------------------------- //

    private static final ArrayList<BundledRedstoneOutputChangedEvent.Listener> listeners = new ArrayList<>();

    private BundledRedstoneAPI() {
    }
}
