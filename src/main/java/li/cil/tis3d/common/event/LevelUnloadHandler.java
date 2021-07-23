package li.cil.tis3d.common.event;

import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

public final class LevelUnloadHandler {
    public static void initialize() {
        MinecraftForge.EVENT_BUS.addListener(LevelUnloadHandler::onLevelUnload);
    }

    // --------------------------------------------------------------------- //

    private static void onLevelUnload(final WorldEvent.Unload event) {
        if (event.getWorld() instanceof Level level) {
            // TODO
//            final ChunkSource chunkSource = level.getChunkSource();
//            if (chunkSource instanceof ServerChunkCache serverChunkCache) {
//                serverChunkCache.
//            }
//            for (final BlockEntity tileEntity : ((Level) event.getWorld()).blockEntityList) {
//                if (tileEntity instanceof CasingTileEntity) {
//                    final CasingTileEntity tileEntityCasing = (CasingTileEntity) tileEntity;
//                    final CasingImpl casing = (CasingImpl) tileEntityCasing.getCasing();
//                    casing.onDisposed();
//                }
//            }
        }
    }
}
