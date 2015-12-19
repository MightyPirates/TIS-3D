package li.cil.tis3d.common.integration.redlogic;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.BundledRedstone;
import mods.immibis.redlogic.api.wiring.IBundledUpdatable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public final class RedLogicCallback {
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

    private RedLogicCallback() {
    }
}
