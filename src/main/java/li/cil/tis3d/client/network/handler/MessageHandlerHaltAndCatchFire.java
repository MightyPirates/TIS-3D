package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import li.cil.tis3d.common.network.handler.AbstractMessageHandlerWithLocation;
import li.cil.tis3d.common.network.message.MessageHaltAndCatchFire;
import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.block.entity.BlockEntity;

public final class MessageHandlerHaltAndCatchFire extends AbstractMessageHandlerWithLocation<MessageHaltAndCatchFire> {
    @Override
    protected void onMessageSynchronized(final MessageHaltAndCatchFire message, final PacketContext context) {
        final BlockEntity blockEntity = getBlockEntity(message, context);
        if (!(blockEntity instanceof ControllerBlockEntity)) {
            return;
        }

        final ControllerBlockEntity controller = (ControllerBlockEntity) blockEntity;
        controller.haltAndCatchFire();
    }
}
