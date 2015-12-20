package li.cil.tis3d.common.integration.redlogic;

import cpw.mods.fml.common.Optional;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.Redstone;
import mods.immibis.redlogic.api.wiring.IRedstoneEmitter;
import mods.immibis.redlogic.api.wiring.IRedstoneUpdatable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

@Optional.InterfaceList({
        @Optional.Interface(iface = "mods.immibis.redlogic.api.wiring.IRedstoneEmitter", modid = ProxyRedLogic.MOD_ID),
        @Optional.Interface(iface = "mods.immibis.redlogic.api.wiring.IRedstoneUpdatable", modid = ProxyRedLogic.MOD_ID)
})
public interface RedLogicRedstone extends IRedstoneEmitter, IRedstoneUpdatable {
    Casing getCasing();

    @Optional.Method(modid = ProxyRedLogic.MOD_ID)
    @Override
    default short getEmittedSignalStrength(final int blockFace, final int toDirection) {
        final Module module = getCasing().getModule(Face.VALUES[toDirection]);
        if (module instanceof Redstone) {
            final Redstone redstone = (Redstone) module;

            return redstone.getRedstoneOutput();
        }
        return 0;
    }

    @Optional.Method(modid = ProxyRedLogic.MOD_ID)
    @Override
    default void onRedstoneInputChanged() {
        getCasing().markDirty();
    }
}
