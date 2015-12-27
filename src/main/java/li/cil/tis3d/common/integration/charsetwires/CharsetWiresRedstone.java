package li.cil.tis3d.common.integration.charsetwires;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.Redstone;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional;
import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.IRedstoneUpdatable;
import pl.asie.charset.api.wires.WireFace;

@Optional.InterfaceList({
        @Optional.Interface(iface = "pl.asie.charset.api.wires.IRedstoneEmitter", modid = ProxyCharsetWires.MOD_ID),
        @Optional.Interface(iface = "pl.asie.charset.api.wires.IRedstoneUpdatable", modid = ProxyCharsetWires.MOD_ID)
})
public interface CharsetWiresRedstone extends IRedstoneEmitter, IRedstoneUpdatable {
    TileEntityCasing getTileEntity();

    @Optional.Method(modid = ProxyCharsetWires.MOD_ID)
    @Override
    default int getRedstoneSignal(final WireFace wireFace, final EnumFacing facing) {
        final Module module = getTileEntity().getModule(Face.fromEnumFacing(facing));
        if (module instanceof Redstone) {
            final Redstone redstone = (Redstone) module;

            return redstone.getRedstoneOutput();
        }
        return 0;
    }

    @Optional.Method(modid = ProxyCharsetWires.MOD_ID)
    @Override
    default void onRedstoneInputChanged(final EnumFacing facing) {
        getTileEntity().markRedstoneDirty();
    }
}
