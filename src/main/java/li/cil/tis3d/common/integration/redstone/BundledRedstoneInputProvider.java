package li.cil.tis3d.common.integration.redstone;

import li.cil.tis3d.api.module.BundledRedstone;

/**
 * Signature of methods that can be queried to compute bundled redstone input.
 */
@FunctionalInterface
public interface BundledRedstoneInputProvider {
    int getBundledInput(BundledRedstone module, int channel);
}
