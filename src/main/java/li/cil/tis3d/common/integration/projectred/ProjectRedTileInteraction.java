package li.cil.tis3d.common.integration.projectred;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.BundledRedstone;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import mrtjp.projectred.api.IBundledTileInteraction;
import mrtjp.projectred.api.ProjectRedAPI;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public final class ProjectRedTileInteraction implements IBundledTileInteraction {
    public static final ProjectRedTileInteraction INSTANCE = new ProjectRedTileInteraction();

    // --------------------------------------------------------------------- //

    public void register() {
        ProjectRedAPI.transmissionAPI.registerBundledTileInteraction(this);
    }

    // --------------------------------------------------------------------- //

    @Override
    public boolean isValidInteractionFor(final World world, final int x, final int y, final int z) {
        return world.blockExists(x, y, z) && world.getTileEntity(x, y, z) instanceof TileEntityCasing;
    }

    @Override
    public boolean canConnectBundled(final World world, final int x, final int y, final int z, final int side) {
        final EnumFacing facing = EnumFacing.getFront(side);
        if (world.blockExists(x, y, z)) {
            final TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityCasing) {
                final TileEntityCasing casing = (TileEntityCasing) tileEntity;

                final Module module = casing.getModule(Face.fromEnumFacing(facing));
                return module instanceof BundledRedstone;
            }
        }
        return false;
    }

    @Override
    public byte[] getBundledSignal(final World world, final int x, final int y, final int z, final int side) {
        final EnumFacing facing = EnumFacing.getFront(side);
        if (world.blockExists(x, y, z)) {
            final TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityCasing) {
                final TileEntityCasing casing = (TileEntityCasing) tileEntity;

                final Module module = casing.getModule(Face.fromEnumFacing(facing));
                if (module instanceof BundledRedstone) {
                    final BundledRedstone bundledRedstone = (BundledRedstone) module;

                    final byte[] signal = new byte[16];
                    for (int channel = 0; channel < signal.length; channel++) {
                        signal[channel] = (byte) bundledRedstone.getBundledRedstoneOutput(channel);
                    }
                    return signal;
                }
            }
        }

        return null;
    }

    // --------------------------------------------------------------------- //

    private ProjectRedTileInteraction() {
    }
}
