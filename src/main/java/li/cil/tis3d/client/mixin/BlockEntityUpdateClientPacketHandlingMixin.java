package li.cil.tis3d.client.mixin;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.block.entity.AbstractComputerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.BlockEntityUpdateClientPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class BlockEntityUpdateClientPacketHandlingMixin {
    @Shadow
    private MinecraftClient client;

    @Inject(method = "onBlockEntityUpdate", at = @At("RETURN"))
    private void onComputerBlockEntityUpdate(final BlockEntityUpdateClientPacket packet, final CallbackInfo ci) {
        if (packet.getActionId() != Constants.BLOCK_ENTITY_ACTION_ID) {
            return;
        }

        final BlockEntity blockEntity = client.world.getBlockEntity(packet.getPos());
        if (blockEntity instanceof AbstractComputerBlockEntity) {
            blockEntity.fromTag(packet.getCompoundTag());
        }
    }
}
