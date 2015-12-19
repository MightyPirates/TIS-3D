package li.cil.tis3d.common.integration.charsetwires;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.BundledRedstone;
import li.cil.tis3d.api.module.Redstone;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import pl.asie.charset.api.wires.IBundledEmitter;
import pl.asie.charset.api.wires.IBundledUpdatable;
import pl.asie.charset.api.wires.IConnectable;
import pl.asie.charset.api.wires.IRedstoneEmitter;
import pl.asie.charset.api.wires.WireFace;
import pl.asie.charset.api.wires.WireType;

import java.util.Arrays;

public final class CharsetWiresCallbacks {
    public static void onBundledOutputChanged(final BundledRedstone module, final int channel) {
        final World world = module.getCasing().getCasingWorld();
        final BlockPos neighborPos = module.getCasing().getPosition().offset(Face.toEnumFacing(module.getFace()));
        if (world.isBlockLoaded(neighborPos)) {
            final TileEntity tileEntity = world.getTileEntity(neighborPos);

            if (tileEntity instanceof IBundledUpdatable) {
                final IBundledUpdatable updatable = (IBundledUpdatable) tileEntity;

                updatable.onBundledInputChanged(Face.toEnumFacing(module.getFace().getOpposite()));
            }
        }
    }

    public static int getInput(final Redstone module) {
        final EnumFacing facing = Face.toEnumFacing(module.getFace());
        final World world = module.getCasing().getCasingWorld();
        final BlockPos inputPos = module.getCasing().getPosition().offset(facing);
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
                return maxSignal;
            }
        }

        return 0;
    }

    public static int getBundledInput(final BundledRedstone module, final int channel) {
        final EnumFacing facing = Face.toEnumFacing(module.getFace());
        final World world = module.getCasing().getCasingWorld();
        final BlockPos inputPos = module.getCasing().getPosition().offset(facing);
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

                int maxSignal = 0;
                for (final WireFace face : WireFace.VALUES) {
                    if (!connectivity[face.ordinal()]) {
                        continue;
                    }

                    final byte[] signal = emitter.getBundledSignal(face, facing.getOpposite());
                    if (signal != null) {
                        if ((signal[channel] & 0xFF) > maxSignal) {
                            maxSignal = signal[channel] & 0xFF;
                        }
                    }
                }
                return maxSignal;
            }
        }

        return 0;
    }

    // --------------------------------------------------------------------- //

    private CharsetWiresCallbacks() {
    }
}
