package li.cil.tis3d.common.fabric;

import li.cil.tis3d.common.CommonBootstrap;
import li.cil.tis3d.common.CommonSetup;
import li.cil.tis3d.common.block.entity.fabric.ChunkUnloadListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class CommonBootstrapFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CommonBootstrap.run();
        CommonSetup.run();

        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            for (final BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                if (blockEntity instanceof final ChunkUnloadListener listener) {
                    listener.onChunkUnloaded();
                }
            }
        });
    }
}
