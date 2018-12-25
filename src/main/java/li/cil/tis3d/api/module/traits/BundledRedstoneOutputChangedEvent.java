package li.cil.tis3d.api.module.traits;

/**
 * Dispatch this on the TODO listener bus
 * when the bundled output of a {@link BundledRedstone} capable module changes.
 * This will then be forwarded to all present bundled redstone APIs to allow
 * reacting to the new values; this means the module should be in a state where
 * querying {@link BundledRedstone#getBundledRedstoneOutput(int)} will already
 * return the new value.
 */
public class BundledRedstoneOutputChangedEvent {
    public interface Listener {
        void onBundledRedstoneOutputChanged(BundledRedstoneOutputChangedEvent event);
    }

    private final BundledRedstone module;
    private final int channel;

    // --------------------------------------------------------------------- //

    public BundledRedstoneOutputChangedEvent(final BundledRedstone module, final int channel) {
        this.module = module;
        this.channel = channel;
    }

    // --------------------------------------------------------------------- //

    /**
     * The module of which the output changed.
     *
     * @return the module that changed its output.
     */
    public BundledRedstone getModule() {
        return module;
    }

    /**
     * The channel of which the signal changed.
     * <p>
     * A negative value indicates that <em>all</em> channels need to be
     * checked, since multiple channels may have changed.
     *
     * @return the channel that now has a different signal.
     */
    public int getChannel() {
        return channel;
    }
}
