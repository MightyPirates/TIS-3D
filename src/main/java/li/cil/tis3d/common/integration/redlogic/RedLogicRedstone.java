package li.cil.tis3d.common.integration.redlogic;

import cpw.mods.fml.common.Optional;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.Redstone;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import mods.immibis.redlogic.api.wiring.IRedstoneEmitter;
import mods.immibis.redlogic.api.wiring.IRedstoneUpdatable;

@Optional.InterfaceList({@Optional.Interface(iface = "mods.immibis.redlogic.api.wiring.IRedstoneEmitter", modid = ProxyRedLogic.MOD_ID),
                         @Optional.Interface(iface = "mods.immibis.redlogic.api.wiring.IRedstoneUpdatable", modid = ProxyRedLogic.MOD_ID)})
public interface RedLogicRedstone extends IRedstoneEmitter, IRedstoneUpdatable {
    TileEntityCasing getTileEntity();

    @Optional.Method(modid = ProxyRedLogic.MOD_ID)
    @Override
    default short getEmittedSignalStrength(final int blockFace, final int toDirection) {
        final Module module = getTileEntity().getModule(Face.VALUES[toDirection]);
        if (module instanceof Redstone) {
            final Redstone redstone = (Redstone) module;

            return redstone.getRedstoneOutput();
        }
        return 0;
    }

    @Optional.Method(modid = ProxyRedLogic.MOD_ID)
    @Override
    default void onRedstoneInputChanged() {
        getTileEntity().markRedstoneDirty();
    }
}
