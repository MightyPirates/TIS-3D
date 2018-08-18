package li.cil.tis3d.common.integration.redstone;

import li.cil.tis3d.api.module.traits.BundledRedstone;

/**
 * Signature for callbacks notified when bundled redstone output of a
 * {@link BundledRedstone} module changes.
 */
@FunctionalInterface
public interface BundledRedstoneOutputChangedCallback {
    void onBundledRedstoneOutputChanged(final BundledRedstone module, final int channel);
}
