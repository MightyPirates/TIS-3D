package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.charset.NetworkContext;
import li.cil.tis3d.common.block.entity.TileEntityCasing;
import li.cil.tis3d.common.network.handler.AbstractMessageHandlerWithLocation;
import li.cil.tis3d.common.network.message.MessageCasingLockedState;
import net.minecraft.block.entity.BlockEntity;

public final class MessageHandlerCasingLockedState extends AbstractMessageHandlerWithLocation<MessageCasingLockedState> {
    @Override
    protected void onMessageSynchronized(final MessageCasingLockedState message, final NetworkContext context) {
        final BlockEntity tileEntity = getTileEntity(message, context);
        if (!(tileEntity instanceof TileEntityCasing)) {
            return;
        }

        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        casing.setCasingLockedClient(message.isLocked());
    }
}
