package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.network.handler.AbstractMessageHandlerWithLocation;
import li.cil.tis3d.common.network.message.PipeLockedStateMessage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.block.entity.BlockEntity;

@Environment(EnvType.CLIENT)
public final class PipeLockedStateMessageHandler extends AbstractMessageHandlerWithLocation<PipeLockedStateMessage> {
    @Override
    protected void onMessageSynchronized(final PipeLockedStateMessage message, final PacketContext context) {
        final BlockEntity blockEntity = getBlockEntity(message, context);
        if (!(blockEntity instanceof CasingBlockEntity)) {
            return;
        }

        final CasingBlockEntity casing = (CasingBlockEntity)blockEntity;
        casing.setReceivingPipeLockedClient(message.getFace(), message.getPort(), message.isLocked());
    }
}
