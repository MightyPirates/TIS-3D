package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.common.network.handler.AbstractMessageHandlerWithLocation;
import li.cil.tis3d.common.network.message.MessagePortLockedState;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageHandlerPortLockedState extends AbstractMessageHandlerWithLocation<MessagePortLockedState> {
    @Override
    public IMessage onMessage(final MessagePortLockedState message, final MessageContext context) {
        final TileEntity tileEntity = getTileEntity(message, context);
        if (!(tileEntity instanceof TileEntityCasing)) {
            return null;
        }

        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        casing.setPortLockedClient(message.getFace(), message.getPort(), message.isLocked());

        return null;
    }
}
