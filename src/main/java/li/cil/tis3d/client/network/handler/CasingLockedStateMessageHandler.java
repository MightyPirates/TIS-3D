package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.network.handler.AbstractMessageHandlerWithLocation;
import li.cil.tis3d.common.network.message.CasingLockedStateMessage;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.block.entity.BlockEntity;

public final class CasingLockedStateMessageHandler extends AbstractMessageHandlerWithLocation<CasingLockedStateMessage> {
    @Override
    protected void onMessageSynchronized(final CasingLockedStateMessage message, final PacketContext context) {
        final BlockEntity blockEntity = getBlockEntity(message, context);
        if (!(blockEntity instanceof CasingBlockEntity)) {
            return;
        }

        final CasingBlockEntity casing = (CasingBlockEntity)blockEntity;
        casing.setCasingLockedClient(message.isLocked());
    }
}
