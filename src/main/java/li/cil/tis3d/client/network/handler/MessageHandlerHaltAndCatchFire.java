package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.charset.NetworkContext;
import li.cil.tis3d.common.block.entity.TileEntityController;
import li.cil.tis3d.common.network.handler.AbstractMessageHandlerWithLocation;
import li.cil.tis3d.common.network.message.MessageHaltAndCatchFire;
import net.minecraft.block.entity.BlockEntity;

public final class MessageHandlerHaltAndCatchFire extends AbstractMessageHandlerWithLocation<MessageHaltAndCatchFire> {
    @Override
    protected void onMessageSynchronized(final MessageHaltAndCatchFire message, final NetworkContext context) {
        final BlockEntity tileEntity = getTileEntity(message, context);
        if (!(tileEntity instanceof TileEntityController)) {
            return;
        }

        final TileEntityController controller = (TileEntityController) tileEntity;
        controller.haltAndCatchFire();
    }
}
