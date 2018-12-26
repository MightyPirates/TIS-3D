package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.common.block.entity.TileEntityCasing;
import li.cil.tis3d.common.network.handler.AbstractMessageHandlerWithLocation;
import li.cil.tis3d.common.network.message.MessageCasingInventory;
import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.block.entity.BlockEntity;

public final class MessageHandlerCasingInventory extends AbstractMessageHandlerWithLocation<MessageCasingInventory> {
    @Override
    protected void onMessageSynchronized(final MessageCasingInventory message, final PacketContext context) {
        final BlockEntity tileEntity = getTileEntity(message, context);
        if (!(tileEntity instanceof TileEntityCasing)) {
            return;
        }

        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        casing.setStackAndModuleClient(message.getSlot(), message.getStack(), message.getModuleData());
    }
}
