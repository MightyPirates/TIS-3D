package li.cil.tis3d.common.entity.neoforge;

import li.cil.tis3d.api.neoforge.Capabilities;
import li.cil.tis3d.common.entity.InfraredPacketEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nullable;

public final class InfraredPacketEntityImpl {
    public static void onPlatformBlockCollision(final InfraredPacketEntity packet, final BlockHitResult hit, @Nullable final BlockEntity blockEntity) {
        if (blockEntity != null && blockEntity.getLevel() != null) {
            final var capability = blockEntity.getLevel().getCapability(Capabilities.INFRARED_RECEIVER_BLOCK, blockEntity.getBlockPos(), hit.getDirection());
            if (capability != null) {
                capability.onInfraredPacket(packet, hit);
            }
        }
    }

    public static void onPlatformEntityCollision(final InfraredPacketEntity packet, final EntityHitResult hit) {
        final var capability = hit.getEntity().getCapability(Capabilities.INFRARED_RECEIVER_ENTITY);
        if (capability != null) {
            capability.onInfraredPacket(packet, hit);
        }
    }
}
