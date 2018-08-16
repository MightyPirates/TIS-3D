package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.common.network.handler.AbstractMessageHandlerWithLocation;
import li.cil.tis3d.common.network.message.MessageCasingEnabledState;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.tileentity.TileEntity;
import pl.asie.protocharset.rift.network.NetworkContext;

public final class MessageHandlerCasingEnabledState extends AbstractMessageHandlerWithLocation<MessageCasingEnabledState> {
    @Override
    protected void onMessageSynchronized(final MessageCasingEnabledState message, final NetworkContext context) {
        final TileEntity tileEntity = getTileEntity(message, context);
        if (!(tileEntity instanceof TileEntityCasing)) {
            return;
        }

        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        casing.setEnabledClient(message.isEnabled());
    }
}
