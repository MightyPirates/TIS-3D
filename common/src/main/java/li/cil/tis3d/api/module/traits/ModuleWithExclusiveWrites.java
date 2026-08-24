/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.api.module.traits;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;

/**
 * Modules implementing this offer one logical value on several ports at once, expecting it to be
 * read exactly once, and cancel the remaining writes in {@link Module#onBeforeWriteComplete(Port)}.
 * <p>
 * Without arbitration, the port a value ends up going to is decided by whichever neighboring module
 * happens to read first. Which is the order the {@link Casing} steps its modules in, which in turn
 * depends on the orientation of the casing in the world. This trait makes the choice a property of
 * the module instead, by pruning the offer down to a single port before any module can read. This
 * ensures locally deterministic behavior, i.e. the same configuration of modules behaves the same
 * regardless of "rotation" in the world.
 */
public interface ModuleWithExclusiveWrites extends Module {
    /**
     * Cancel all but the highest priority write that could be read in the coming step.
     * <p>
     * Called after all pipes have been stepped, before any module is stepped again.
     */
    default void arbitrateWrites() {
        boolean claimed = false;
        for (final Port port : Port.VALUES) {
            final Pipe pipe = getCasing().getSendingPipe(getFace(), port);
            if (!pipe.canTransfer()) {
                continue;
            }

            if (claimed) {
                pipe.cancelWrite();
            } else {
                claimed = true;
            }
        }
    }
}
