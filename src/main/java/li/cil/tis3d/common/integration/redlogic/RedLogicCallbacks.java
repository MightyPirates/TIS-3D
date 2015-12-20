package li.cil.tis3d.common.integration.redlogic;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.BundledRedstone;
import li.cil.tis3d.api.module.Redstone;
import mods.immibis.redlogic.api.wiring.IBundledEmitter;
import mods.immibis.redlogic.api.wiring.IBundledUpdatable;
import mods.immibis.redlogic.api.wiring.IRedstoneEmitter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public final class RedLogicCallbacks {
    public static void onBundledOutputChanged(final BundledRedstone module, final int channel) {
        final World world = module.getCasing().getCasingWorld();
        final EnumFacing facing = Face.toEnumFacing(module.getFace());
        final int neighborX = module.getCasing().getPositionX() + facing.getFrontOffsetX();
        final int neighborY = module.getCasing().getPositionY() + facing.getFrontOffsetY();
        final int neighborZ = module.getCasing().getPositionZ() + facing.getFrontOffsetZ();
        if (world.blockExists(neighborX, neighborY, neighborZ)) {
            final TileEntity tileEntity = world.getTileEntity(neighborX, neighborY, neighborZ);

            if (tileEntity instanceof IBundledUpdatable) {
                final IBundledUpdatable updatable = (IBundledUpdatable) tileEntity;

                updatable.onBundledInputChanged();
            }
        }
    }

    public static int getInput(final Redstone module) {
        final EnumFacing facing = Face.toEnumFacing(module.getFace());
        final World world = module.getCasing().getCasingWorld();
        final int inputX = module.getCasing().getPositionX() + facing.getFrontOffsetX();
        final int inputY = module.getCasing().getPositionY() + facing.getFrontOffsetY();
        final int inputZ = module.getCasing().getPositionZ() + facing.getFrontOffsetZ();
        if (world.blockExists(inputX, inputY, inputZ)) {
            final TileEntity tileEntity = world.getTileEntity(inputX, inputY, inputZ);

            if (tileEntity instanceof IRedstoneEmitter) {
                final IRedstoneEmitter emitter = (IRedstoneEmitter) tileEntity;

                short maxSignal = 0;
                for (int blockFace = 0; blockFace < 6; blockFace++) {
                    final short signal = emitter.getEmittedSignalStrength(blockFace, module.getFace().getOpposite().ordinal());
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
        final int inputX = module.getCasing().getPositionX() + facing.getFrontOffsetX();
        final int inputY = module.getCasing().getPositionY() + facing.getFrontOffsetY();
        final int inputZ = module.getCasing().getPositionZ() + facing.getFrontOffsetZ();
        if (world.blockExists(inputX, inputY, inputZ)) {
            final TileEntity tileEntity = world.getTileEntity(inputX, inputY, inputZ);

            if (tileEntity instanceof IBundledEmitter) {
                final IBundledEmitter emitter = (IBundledEmitter) tileEntity;

                int maxSignal = 0;
                for (int blockFace = 0; blockFace < 6; blockFace++) {
                    final byte[] signal = emitter.getBundledCableStrength(blockFace, module.getFace().getOpposite().ordinal());
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

    private RedLogicCallbacks() {
    }
}
