package li.cil.tis3d.common.machine;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Abstraction layer for pipe containers, provides positional awareness.
 */
public interface PipeHost {
    World getPipeHostWorld();

    BlockPos getPipeHostPosition();

    void onWriteComplete(Face sendingFace, Port sendingPort);
}
