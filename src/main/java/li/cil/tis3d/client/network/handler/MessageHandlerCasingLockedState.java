package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.common.network.handler.AbstractMessageHandlerWithLocation;
import li.cil.tis3d.common.network.message.MessageCasingLockedState;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.tileentity.TileEntity;
import pl.asie.protocharset.rift.network.NetworkContext;

public final class MessageHandlerCasingLockedState extends AbstractMessageHandlerWithLocation<MessageCasingLockedState> {
    @Override
    protected void onMessageSynchronized(final MessageCasingLockedState message, final NetworkContext context) {
        final TileEntity tileEntity = getTileEntity(message, context);
        if (!(tileEntity instanceof TileEntityCasing)) {
            return;
        }

        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        casing.setCasingLockedClient(message.isLocked());
    }
}
