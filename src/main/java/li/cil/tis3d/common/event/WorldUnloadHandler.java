package li.cil.tis3d.common.event;

import li.cil.tis3d.common.machine.CasingImpl;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class WorldUnloadHandler {
    public static final WorldUnloadHandler INSTANCE = new WorldUnloadHandler();

    // --------------------------------------------------------------------- //

    @SubscribeEvent
    public void onWorldUnload(final WorldEvent.Unload event) {
        for (final TileEntity tileEntity : event.getWorld().loadedTileEntityList) {
            if (tileEntity instanceof TileEntityCasing) {
                final TileEntityCasing tileEntityCasing = (TileEntityCasing) tileEntity;
                final CasingImpl casing = (CasingImpl) tileEntityCasing.getCasing();
                casing.onDisposed();
            }
        }
    }

    // --------------------------------------------------------------------- //

    private WorldUnloadHandler() {
    }
}
