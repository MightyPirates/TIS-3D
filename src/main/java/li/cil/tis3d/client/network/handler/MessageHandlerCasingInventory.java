package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.charset.NetworkContext;
import li.cil.tis3d.common.block.entity.TileEntityCasing;
import li.cil.tis3d.common.network.handler.AbstractMessageHandlerWithLocation;
import li.cil.tis3d.common.network.message.MessageCasingInventory;
import net.minecraft.block.entity.BlockEntity;

public final class MessageHandlerCasingInventory extends AbstractMessageHandlerWithLocation<MessageCasingInventory> {
    @Override
    protected void onMessageSynchronized(final MessageCasingInventory message, final NetworkContext context) {
        final BlockEntity tileEntity = getTileEntity(message, context);
        if (!(tileEntity instanceof TileEntityCasing)) {
            return;
        }

        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        casing.setStackAndModuleClient(message.getSlot(), message.getStack(), message.getModuleData());
    }
}
