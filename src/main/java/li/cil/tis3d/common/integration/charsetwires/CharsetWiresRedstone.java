package li.cil.tis3d.common.integration.charsetwires;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.Redstone;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import pl.asie.charset.api.wires.IConnectable;
import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.IRedstoneUpdatable;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;

import java.util.Arrays;

@Optional.InterfaceList({
        @Optional.Interface(iface = "pl.asie.charset.api.wires.IRedstoneEmitter", modid = ModCharsetWires.MOD_ID),
        @Optional.Interface(iface = "pl.asie.charset.api.wires.IRedstoneUpdatable", modid = ModCharsetWires.MOD_ID)
})
public interface CharsetWiresRedstone extends IRedstoneEmitter, IRedstoneUpdatable {
    Casing getCasing();

    @Optional.Method(modid = ModCharsetWires.MOD_ID)
    @Override
    default int getRedstoneSignal(final WireFace wireFace, final EnumFacing facing) {
        final Module module = getCasing().getModule(Face.fromEnumFacing(facing));
        if (module instanceof Redstone) {
            final Redstone redstone = (Redstone) module;

            return redstone.getRedstoneOutput();
        }
        return 0;
    }

    @Optional.Method(modid = ModCharsetWires.MOD_ID)
    @Override
    default void onRedstoneInputChanged(final EnumFacing facing) {
        final Module module = getCasing().getModule(Face.fromEnumFacing(facing));
        if (module instanceof Redstone) {
            final Redstone redstone = (Redstone) module;

            final World world = getCasing().getCasingWorld();
            final BlockPos inputPos = getCasing().getPosition().offset(facing);
            if (world.isBlockLoaded(inputPos)) {
                final TileEntity tileEntity = world.getTileEntity(inputPos);

                final boolean[] connectivity = new boolean[WireFace.VALUES.length];
                if (tileEntity instanceof IConnectable) {
                    final IConnectable connectable = (IConnectable) tileEntity;
                    for (final WireFace face : WireFace.VALUES) {
                        connectivity[face.ordinal()] = connectable.canConnect(WireType.NORMAL, face, facing.getOpposite());
                    }
                } else {
                    Arrays.fill(connectivity, true);
                }

                if (tileEntity instanceof IRedstoneEmitter) {
                    final IRedstoneEmitter emitter = (IRedstoneEmitter) tileEntity;

                    short maxSignal = 0;
                    for (final WireFace face : WireFace.VALUES) {
                        if (!connectivity[face.ordinal()]) {
                            continue;
                        }

                        final short signal = (short) emitter.getRedstoneSignal(face, facing.getOpposite());
                        if ((signal & 0xFFFF) > (maxSignal & 0xFFFF)) {
                            maxSignal = (short) (signal & 0xFFFF);
                        }
                    }
                    redstone.setRedstoneInput(maxSignal);
                }
            }
        }
    }
}
