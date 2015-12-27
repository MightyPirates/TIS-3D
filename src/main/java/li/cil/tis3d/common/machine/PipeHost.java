package li.cil.tis3d.common.machine;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import net.minecraft.world.World;

/**
 * Abstraction layer for pipe containers, provides positional awareness.
 */
public interface PipeHost {
    World getPipeHostWorld();

    int getPipeHostPositionX();

    int getPipeHostPositionY();

    int getPipeHostPositionZ();

    void onWriteComplete(Face sendingFace, Port sendingPort);
}
