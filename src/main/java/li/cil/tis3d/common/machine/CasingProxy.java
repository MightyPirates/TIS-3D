package li.cil.tis3d.common.machine;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface CasingProxy extends Casing {
    Casing getCasing();

    @Override
    default World getCasingWorld() {
        return getCasing().getCasingWorld();
    }

    @Override
    default BlockPos getPosition() {
        return getCasing().getPosition();
    }

    @Override
    default void markDirty() {
        getCasing().markDirty();
    }

    @Override
    default boolean isLocked() {
        return getCasing().isLocked();
    }

    @Override
    default Module getModule(final Face face) {
        return getCasing().getModule(face);
    }

    @Override
    default Pipe getReceivingPipe(final Face face, final Port port) {
        return getCasing().getReceivingPipe(face, port);
    }

    @Override
    default Pipe getSendingPipe(final Face face, final Port port) {
        return getCasing().getSendingPipe(face, port);
    }

    @Override
    default void sendData(final Face face, final NBTTagCompound data) {
        getCasing().sendData(face, data);
    }
}
