/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.common.entity.fabric;

import li.cil.tis3d.api.fabric.Lookups;
import li.cil.tis3d.common.entity.InfraredPacketEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nullable;

public final class InfraredPacketEntityImpl {
    public static void onPlatformBlockCollision(final InfraredPacketEntity packet, final BlockHitResult hit, @Nullable final BlockEntity blockEntity) {
        if (blockEntity != null && blockEntity.getLevel() != null) {
            final var api = Lookups.INFRARED_RECEIVER_BLOCK.find(blockEntity.getLevel(), blockEntity.getBlockPos(), hit.getDirection());
            if (api != null) {
                api.onInfraredPacket(packet, hit);
            }
        }
    }

    public static void onPlatformEntityCollision(final InfraredPacketEntity packet, final EntityHitResult hit) {
        final var api = Lookups.INFRARED_RECEIVER_ENTITY.find(hit.getEntity(), null);
        if (api != null) {
            api.onInfraredPacket(packet, hit);
        }
    }
}
