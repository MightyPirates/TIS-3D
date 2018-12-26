package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.network.handler.AbstractMessageHandlerWithLocation;
import li.cil.tis3d.common.network.message.PipeLockedStateMessage;
import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.block.entity.BlockEntity;

public final class PipeLockedStateMessageHandler extends AbstractMessageHandlerWithLocation<PipeLockedStateMessage> {
    @Override
    protected void onMessageSynchronized(final PipeLockedStateMessage message, final PacketContext context) {
        final BlockEntity blockEntity = getBlockEntity(message, context);
        if (!(blockEntity instanceof CasingBlockEntity)) {
            return;
        }

        final CasingBlockEntity casing = (CasingBlockEntity) blockEntity;
        casing.setReceivingPipeLockedClient(message.getFace(), message.getPort(), message.isLocked());
    }
}
