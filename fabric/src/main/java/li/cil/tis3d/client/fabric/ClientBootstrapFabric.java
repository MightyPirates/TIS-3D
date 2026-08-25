/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.client.fabric;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import li.cil.tis3d.client.ClientBootstrap;
import li.cil.tis3d.client.ClientSetup;
import li.cil.tis3d.client.renderer.block.fabric.ModuleModelLoader;
import li.cil.tis3d.client.renderer.entity.NullEntityRenderer;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.block.entity.fabric.ChunkUnloadListener;
import li.cil.tis3d.common.entity.Entities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

public final class ClientBootstrapFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientBootstrap.run();
        ClientSetup.run();

        EntityRendererRegistry.register(Entities.INFRARED_PACKET, NullEntityRenderer::new);

        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            for (final BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                if (blockEntity instanceof final ChunkUnloadListener listener) {
                    listener.onChunkUnloaded();
                }
            }
        });

        ClientPickBlockGatherCallback.EVENT.register((player, result) -> {
            // Allow picking modules installed in the casing.
            if (result instanceof final BlockHitResult hit) {
                final BlockEntity blockEntity = player.level().getBlockEntity(hit.getBlockPos());
                if (blockEntity instanceof final CasingBlockEntity casing) {
                    final var stack = casing.getItem(casing.toLocal(hit.getDirection()).ordinal());
                    if (!stack.isEmpty()) {
                        return stack.copy();
                    }
                }
            }
            return ItemStack.EMPTY;
        });

        ModuleModelLoader.initialize();
    }
}
