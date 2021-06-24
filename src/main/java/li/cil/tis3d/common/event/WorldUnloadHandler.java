package li.cil.tis3d.common.event;

import li.cil.tis3d.common.machine.CasingImpl;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

public final class WorldUnloadHandler {
    public static void initialize() {
        MinecraftForge.EVENT_BUS.addListener(WorldUnloadHandler::onWorldUnload);
    }

    // --------------------------------------------------------------------- //

    private static void onWorldUnload(final WorldEvent.Unload event) {
        if (event.getWorld() instanceof World) {
            for (final TileEntity tileEntity : ((World) event.getWorld()).loadedTileEntityList) {
                if (tileEntity instanceof TileEntityCasing) {
                    final TileEntityCasing tileEntityCasing = (TileEntityCasing) tileEntity;
                    final CasingImpl casing = (CasingImpl) tileEntityCasing.getCasing();
                    casing.onDisposed();
                }
            }
        }
    }
}
