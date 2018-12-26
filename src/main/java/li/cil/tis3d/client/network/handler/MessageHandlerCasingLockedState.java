package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.network.handler.AbstractMessageHandlerWithLocation;
import li.cil.tis3d.common.network.message.MessageCasingLockedState;
import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.block.entity.BlockEntity;

public final class MessageHandlerCasingLockedState extends AbstractMessageHandlerWithLocation<MessageCasingLockedState> {
    @Override
    protected void onMessageSynchronized(final MessageCasingLockedState message, final PacketContext context) {
        final BlockEntity blockEntity = getBlockEntity(message, context);
        if (!(blockEntity instanceof CasingBlockEntity)) {
            return;
        }

        final CasingBlockEntity casing = (CasingBlockEntity) blockEntity;
        casing.setCasingLockedClient(message.isLocked());
    }
}
