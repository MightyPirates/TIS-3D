package li.cil.tis3d.common.integration.redstone;

import li.cil.tis3d.api.module.traits.BundledRedstone;
import li.cil.tis3d.api.module.traits.BundledRedstoneOutputChangedEvent;
import li.cil.tis3d.api.module.traits.Redstone;
import li.cil.tis3d.common.TIS3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Glue for notifying specific bundled redstone APIs of changes.
 */
public enum RedstoneIntegration implements BundledRedstoneOutputChangedEvent.Listener {
    INSTANCE;

    // --------------------------------------------------------------------- //

    private final List<BundledRedstoneOutputChangedCallback> callbacks = new ArrayList<>();
    private final List<RedstoneInputProvider> redstoneInputProviders = new ArrayList<>();
    private final List<BundledRedstoneInputProvider> bundledRedstoneInputProviders = new ArrayList<>();

    // --------------------------------------------------------------------- //

    public void addCallback(final BundledRedstoneOutputChangedCallback callback) {
        callbacks.add(callback);
    }

    public void addRedstoneInputProvider(final RedstoneInputProvider provider) {
        redstoneInputProviders.add(provider);
    }

    public void addBundledRedstoneInputProvider(final BundledRedstoneInputProvider provider) {
        bundledRedstoneInputProviders.add(provider);
    }

    public int getRedstoneInput(final Redstone module) {
        int maxSignal = 0;
        for (final RedstoneInputProvider provider : redstoneInputProviders) {
            try {
                final int signal = provider.getInput(module);
                if (signal > maxSignal) {
                    maxSignal = signal;
                }
            } catch (final Throwable t) {
                TIS3D.getLog().warn("Redstone input provider derped!", t);
            }
        }
        return maxSignal;
    }

    public int getBundledRedstoneInput(final BundledRedstone module, final int channel) {
        int maxSignal = 0;
        for (final BundledRedstoneInputProvider provider : bundledRedstoneInputProviders) {
            try {
                final int signal = provider.getBundledInput(module, channel);
                if (signal > maxSignal) {
                    maxSignal = signal;
                }
            } catch (final Throwable t) {
                TIS3D.getLog().warn("Bundled redstone input provider derped!", t);
            }
        }
        return maxSignal;
    }

    // --------------------------------------------------------------------- //

    @Override
    public void onBundledRedstoneOutputChanged(final BundledRedstoneOutputChangedEvent event) {
        final BundledRedstone module = event.getModule();
        final int channel = event.getChannel();

        for (final BundledRedstoneOutputChangedCallback callback : callbacks) {
            callback.onBundledRedstoneOutputChanged(module, channel);
        }
    }
}
