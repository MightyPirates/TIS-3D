package li.cil.tis3d.common.event;

// TODO
public final class WorldUnloadHandler {
    public static final WorldUnloadHandler INSTANCE = new WorldUnloadHandler();

    // --------------------------------------------------------------------- //

    /* @SubscribeEvent
    public void onWorldUnload(final WorldEvent.Unload event) {
        for (final TileEntity tileEntity : event.getWorld().loadedTileEntityList) {
            if (tileEntity instanceof TileEntityCasing) {
                final TileEntityCasing tileEntityCasing = (TileEntityCasing) tileEntity;
                final CasingImpl casing = (CasingImpl) tileEntityCasing.getCasing();
                casing.onDisposed();
            }
        }
    } */

    // --------------------------------------------------------------------- //

    private WorldUnloadHandler() {
    }
}
