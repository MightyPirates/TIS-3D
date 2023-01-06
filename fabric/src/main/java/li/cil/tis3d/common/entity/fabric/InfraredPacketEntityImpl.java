package li.cil.tis3d.common.entity.fabric;

import li.cil.tis3d.common.entity.InfraredPacketEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nullable;

public final class InfraredPacketEntityImpl {
    public static void onPlatformBlockCollision(final InfraredPacketEntity packet, final BlockHitResult hit, @Nullable final BlockEntity blockEntity) {
    }

    public static void onPlatformEntityCollision(final InfraredPacketEntity packet, final EntityHitResult hit) {
    }
}
