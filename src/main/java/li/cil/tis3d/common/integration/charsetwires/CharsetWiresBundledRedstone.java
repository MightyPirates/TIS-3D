package li.cil.tis3d.common.integration.charsetwires;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.BundledRedstone;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledUpdatable;
import pl.asie.charset.api.wires.WireFace;

@Optional.InterfaceList({
        @Optional.Interface(iface = "pl.asie.charset.api.wires.IBundledEmitter", modid = ProxyCharsetWires.MOD_ID),
        @Optional.Interface(iface = "pl.asie.charset.api.wires.IBundledUpdatable", modid = ProxyCharsetWires.MOD_ID)
})
public interface CharsetWiresBundledRedstone extends IBundledEmitter, IBundledUpdatable {
    TileEntityCasing getTileEntity();

    @Optional.Method(modid = ProxyCharsetWires.MOD_ID)
    @Override
    default byte[] getBundledSignal(final WireFace wireFace, final EnumFacing facing) {
        final Module module = getTileEntity().getModule(Face.fromEnumFacing(facing));
        if (module instanceof BundledRedstone) {
            final BundledRedstone bundledRedstone = (BundledRedstone) module;

            final byte[] signal = new byte[16];
            for (int channel = 0; channel < signal.length; channel++) {
                signal[channel] = (byte) bundledRedstone.getBundledRedstoneOutput(channel);
            }
            return signal;
        }
        return null;
    }

    @Optional.Method(modid = ProxyCharsetWires.MOD_ID)
    @Override
    default void onBundledInputChanged(final EnumFacing facing) {
        getTileEntity().markRedstoneDirty();
    }
}
