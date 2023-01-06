package li.cil.tis3d.common.entity.forge;

import li.cil.tis3d.api.infrared.InfraredReceiver;
import li.cil.tis3d.common.capabilities.Capabilities;
import li.cil.tis3d.common.entity.InfraredPacketEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public final class InfraredPacketEntityImpl {
    public static void onPlatformBlockCollision(final InfraredPacketEntity packet, final BlockHitResult hit, @Nullable final BlockEntity blockEntity) {
        if (blockEntity != null) {
            final LazyOptional<InfraredReceiver> capability = blockEntity.getCapability(Capabilities.INFRARED_RECEIVER, hit.getDirection());
            capability.ifPresent(receiver -> receiver.onInfraredPacket(packet, hit));
        }
    }

    public static void onPlatformEntityCollision(final InfraredPacketEntity packet, final EntityHitResult hit) {
        final LazyOptional<InfraredReceiver> capability = hit.getEntity().getCapability(Capabilities.INFRARED_RECEIVER, null);
        capability.ifPresent(receiver -> receiver.onInfraredPacket(packet, hit));
    }
}
