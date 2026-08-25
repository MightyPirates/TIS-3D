/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.common.machine;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

/**
 * Abstraction layer for pipe containers, provides positional awareness.
 */
public interface PipeHost {
    Level getPipeHostLevel();

    BlockPos getPipeHostPosition();

    default Rotation getPipeHostRotation() {
        return Rotation.NONE;
    }

    void onPipeStateChanged();

    default void onBeforeWriteComplete(final Face sendingFace, final Port sendingPort) {
    }

    default void onWriteComplete(final Face sendingFace, final Port sendingPort) {
    }
}
