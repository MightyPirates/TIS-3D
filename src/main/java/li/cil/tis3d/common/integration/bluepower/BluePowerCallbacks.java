package li.cil.tis3d.common.integration.bluepower;

import com.bluepowermod.api.BPApi;
import com.bluepowermod.api.wire.redstone.IBundledDevice;
import com.bluepowermod.api.wire.redstone.IRedstoneDevice;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.traits.BundledRedstone;
import li.cil.tis3d.api.module.traits.Redstone;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public final class BluePowerCallbacks {
    public static void onBundledOutputChanged(final BundledRedstone module, final int channel) {
        final EnumFacing facing = Face.toEnumFacing(module.getFace());
        final World world = module.getCasing().getCasingWorld();
        final int neighborX = module.getCasing().getPositionX() + facing.getFrontOffsetX();
        final int neighborY = module.getCasing().getPositionY() + facing.getFrontOffsetY();
        final int neighborZ = module.getCasing().getPositionZ() + facing.getFrontOffsetZ();
        if (world.blockExists(neighborX, neighborY, neighborZ)) {
            for (final ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
                final IRedstoneDevice device = BPApi.getInstance().getRedstoneApi().getRedstoneDevice(world, neighborX, neighborY, neighborZ, face, ForgeDirection.UNKNOWN);
                if (device != null) {
                    device.onRedstoneUpdate();
                }

                final IBundledDevice bundledDevice = BPApi.getInstance().getRedstoneApi().getBundledDevice(world, neighborX, neighborY, neighborZ, face, ForgeDirection.UNKNOWN);
                if (bundledDevice != null) {
                    bundledDevice.onBundledUpdate();
                }
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
            int maxSignal = 0;
            for (final ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
                final IRedstoneDevice device = BPApi.getInstance().getRedstoneApi().getRedstoneDevice(world, inputX, inputY, inputZ, face, ForgeDirection.UNKNOWN);
                if (device != null) {
                    final int signal = device.getRedstonePower(Face.toForgeDirection(module.getFace().getOpposite())) & 0xFF;
                    if (signal > maxSignal) {
                        maxSignal = signal;
                    }
                }
            }
            return maxSignal;
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
            int maxSignal = 0;
            for (final ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
                final IBundledDevice device = BPApi.getInstance().getRedstoneApi().getBundledDevice(world, inputX, inputY, inputZ, face, ForgeDirection.UNKNOWN);
                if (device != null) {
                    final byte[] signal = device.getBundledOutput(Face.toForgeDirection(module.getFace().getOpposite()));
                    if ((signal[channel] & 0xFF) > maxSignal) {
                        maxSignal = signal[channel] & 0xFF;
                    }
                }
            }
            return maxSignal;
        }

        return 0;
    }

    // --------------------------------------------------------------------- //

    private BluePowerCallbacks() {
    }
}
