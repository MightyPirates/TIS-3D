package li.cil.tis3d.common.integration.charsetwires;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.BundledRedstone;
import li.cil.tis3d.api.module.Module;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledUpdatable;
import pl.asie.charset.api.wires.IConnectable;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;

import java.util.Arrays;

@Optional.InterfaceList({
        @Optional.Interface(iface = "pl.asie.charset.api.wires.IBundledEmitter", modid = ModCharsetWires.MOD_ID),
        @Optional.Interface(iface = "pl.asie.charset.api.wires.IBundledUpdatable", modid = ModCharsetWires.MOD_ID)
})
public interface CharsetWiresBundledRedstone extends IBundledEmitter, IBundledUpdatable {
    Casing getCasing();

    @Optional.Method(modid = ModCharsetWires.MOD_ID)
    @Override
    default byte[] getBundledSignal(final WireFace wireFace, final EnumFacing facing) {
        final Module module = getCasing().getModule(Face.fromEnumFacing(facing));
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

    @Optional.Method(modid = ModCharsetWires.MOD_ID)
    @Override
    default void onBundledInputChanged(final EnumFacing facing) {
        final Module module = getCasing().getModule(Face.fromEnumFacing(facing));
        if (module instanceof BundledRedstone) {
            final BundledRedstone bundledRedstone = (BundledRedstone) module;

            final World world = getCasing().getCasingWorld();
            final BlockPos inputPos = getCasing().getPosition().offset(facing);
            if (world.isBlockLoaded(inputPos)) {
                final TileEntity tileEntity = world.getTileEntity(inputPos);

                final boolean[] connectivity = new boolean[WireFace.VALUES.length];
                if (tileEntity instanceof IConnectable) {
                    final IConnectable connectable = (IConnectable) tileEntity;
                    for (final WireFace face : WireFace.VALUES) {
                        connectivity[face.ordinal()] = connectable.canConnect(WireType.BUNDLED, face, facing.getOpposite())
                                || connectable.canConnect(WireType.INSULATED, face, facing.getOpposite());
                    }
                } else {
                    Arrays.fill(connectivity, true);
                }

                if (tileEntity instanceof IBundledEmitter) {
                    final IBundledEmitter emitter = (IBundledEmitter) tileEntity;

                    final byte[] maxSignal = new byte[16];
                    for (final WireFace face : WireFace.VALUES) {
                        if (!connectivity[face.ordinal()]) {
                            continue;
                        }

                        final byte[] signal = emitter.getBundledSignal(face, facing.getOpposite());
                        if (signal != null) {
                            for (int channel = 0; channel < maxSignal.length; channel++) {
                                if (signal[channel] > maxSignal[channel]) {
                                    maxSignal[channel] = signal[channel];
                                }
                            }
                        }
                    }

                    for (int channel = 0; channel < maxSignal.length; channel++) {
                        bundledRedstone.setBundledRedstoneInput(channel, maxSignal[channel]);
                    }
                }
            }
        }
    }
}
