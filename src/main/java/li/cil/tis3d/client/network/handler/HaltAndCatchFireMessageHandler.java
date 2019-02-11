package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import li.cil.tis3d.common.network.handler.AbstractMessageHandlerWithLocation;
import li.cil.tis3d.common.network.message.HaltAndCatchFireMessage;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.block.entity.BlockEntity;

public final class HaltAndCatchFireMessageHandler extends AbstractMessageHandlerWithLocation<HaltAndCatchFireMessage> {
    @Override
    protected void onMessageSynchronized(final HaltAndCatchFireMessage message, final PacketContext context) {
        final BlockEntity blockEntity = getBlockEntity(message, context);
        if (!(blockEntity instanceof ControllerBlockEntity)) {
            return;
        }

        final ControllerBlockEntity controller = (ControllerBlockEntity)blockEntity;
        controller.haltAndCatchFire();
    }
}
