package li.cil.tis3d.common.integration.charsetwires;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.BundledRedstone;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import pl.asie.charset.api.wires.IBundledUpdatable;

public final class CharsetWiresCallback {
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

    private CharsetWiresCallback() {
    }
}
