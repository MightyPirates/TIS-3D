package li.cil.tis3d.common.integration.enderio;

import com.enderio.core.common.util.DyeColor;
import crazypants.enderio.conduit.redstone.ISignalProvider;
import crazypants.enderio.conduit.redstone.InsulatedRedstoneConduit;
import crazypants.enderio.conduit.redstone.Signal;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.BundledRedstone;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum SignalProviderTIS3D implements ISignalProvider {
    INSTANCE;

    // --------------------------------------------------------------------- //

    public static void register() {
        InsulatedRedstoneConduit.addSignalProvider(SignalProviderTIS3D.INSTANCE);
    }

    // --------------------------------------------------------------------- //
    // ISignalProvider

    @Override
    public boolean connectsToNetwork(final World world, final BlockPos pos, final EnumFacing side) {
        return getBundledRedstoneModule(world, pos, side) != null;
    }

    @Override
    public Set<Signal> getNetworkInputs(final World world, final BlockPos pos, final EnumFacing side) {
        final BundledRedstone module = getBundledRedstoneModule(world, pos, side);
        if (module == null) {
            return Collections.emptySet();
        }

        final Set<Signal> result = new HashSet<>();
        for (final DyeColor color : DyeColor.values()) {
            final int strength = module.getBundledRedstoneOutput(color.ordinal());
            if (strength != 0) {
                result.add(new Signal(pos, side, strength, color));
            }
        }
        return result;
    }

    @Nullable
    private static BundledRedstone getBundledRedstoneModule(final World world, final BlockPos pos, final EnumFacing side) {
        if (!world.isBlockLoaded(pos)) {
            return null;
        }

        final TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof TileEntityCasing)) {
            return null;
        }

        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        final Module module = casing.getModule(Face.fromEnumFacing(side));
        if (!(module instanceof BundledRedstone)) {
            return null;
        }

        return (BundledRedstone) module;
    }
}
