package li.cil.tis3d.common.integration.enderio;

import crazypants.enderio.conduit.IConduitBundle;
import crazypants.enderio.conduit.redstone.IRedstoneConduit;
import crazypants.enderio.conduit.redstone.Signal;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.traits.BundledRedstone;
import li.cil.tis3d.common.integration.redstone.RedstoneIntegration;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;

public final class RedstoneInputProviderEnderIO {
    public static void register() {
        RedstoneIntegration.INSTANCE.addBundledRedstoneInputProvider(RedstoneInputProviderEnderIO::getBundledInput);
    }

    // --------------------------------------------------------------------- //

    public static int getBundledInput(final BundledRedstone module, final int channel) {
        final EnumFacing facing = Face.toEnumFacing(module.getFace());
        final World world = module.getCasing().getCasingWorld();
        final BlockPos neighborPos = module.getCasing().getPosition().offset(facing);
        if (world.isBlockLoaded(neighborPos)) {
            final TileEntity tileEntity = world.getTileEntity(neighborPos);
            if (tileEntity instanceof IConduitBundle) {
                final IConduitBundle bundle = (IConduitBundle) tileEntity;
                if (bundle.hasType(IRedstoneConduit.class)) {
                    final IRedstoneConduit conduit = bundle.getConduit(IRedstoneConduit.class);
                    final Collection<Signal> outputs = conduit.getNetworkOutputs(facing.getOpposite());
                    int maxStrength = 0;
                    for (final Signal output : outputs) {
                        if (output.color.ordinal() == channel) {
                            maxStrength = Math.max(maxStrength, output.strength);
                        }
                    }
                    return maxStrength;
                }
            }
        }
        return 0;
    }

    // --------------------------------------------------------------------- //

    private RedstoneInputProviderEnderIO() {
    }
}
