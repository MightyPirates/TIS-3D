package li.cil.tis3d.common.integration.redstone;

import li.cil.tis3d.api.module.traits.Redstone;

/**
 * Signature of methods that can be queried to compute simple redstone input.
 */
@FunctionalInterface
public interface RedstoneInputProvider {
    int getInput(Redstone module);
}
