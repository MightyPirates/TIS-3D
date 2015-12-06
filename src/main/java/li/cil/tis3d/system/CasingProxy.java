package li.cil.tis3d.system;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Pipe;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.api.module.Module;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public interface CasingProxy extends Casing {
    Casing getCasing();

    @Override
    default World getCasingWorld() {
        return getCasing().getCasingWorld();
    }

    @Override
    default int getPositionX() {
        return getCasing().getPositionX();
    }

    @Override
    default int getPositionY() {
        return getCasing().getPositionY();
    }

    @Override
    default int getPositionZ() {
        return getCasing().getPositionZ();
    }

    @Override
    default void markDirty() {
        getCasing().markDirty();
    }

    @Override
    default Module getModule(final Face face) {
        return getCasing().getModule(face);
    }

    @Override
    default void setModule(final Face face, final Module module) {
        getCasing().setModule(face, module);
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
