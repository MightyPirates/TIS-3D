package li.cil.tis3d.common.integration;

import li.cil.tis3d.api.module.BundledRedstone;
import li.cil.tis3d.api.module.BundledRedstoneOutputChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Glue for notifying specific bundled redstone APIs of changes.
 */
public final class RegistryBundledRedstone {
    /**
     * Signature for callbacks notified when bundled redstone output of a
     * {@link BundledRedstone} module changes.
     */
    @FunctionalInterface
    public interface BundledRedstoneOutputChangedCallback {
        void onBundledRedstoneOutputChanged(BundledRedstone module, int channel);
    }

    public static final RegistryBundledRedstone INSTANCE = new RegistryBundledRedstone();

    // --------------------------------------------------------------------- //

    private final List<BundledRedstoneOutputChangedCallback> callbacks = new ArrayList<>();

    // --------------------------------------------------------------------- //

    public void addCallback(final BundledRedstoneOutputChangedCallback callback) {
        callbacks.add(callback);
    }

    @SubscribeEvent
    public void onBundledRedstoneOutputChanged(final BundledRedstoneOutputChangedEvent event) {
        final BundledRedstone module = event.getModule();
        final int channel = event.getChannel();

        for (final BundledRedstoneOutputChangedCallback callback : callbacks) {
            callback.onBundledRedstoneOutputChanged(module, channel);
        }
    }

    // --------------------------------------------------------------------- //

    private RegistryBundledRedstone() {
    }
}
