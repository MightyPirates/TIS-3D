package li.cil.tis3d.client.network.handler;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import li.cil.tis3d.common.network.handler.AbstractMessageHandlerWithLocation;
import li.cil.tis3d.common.network.message.MessageHaltAndCatchFire;
import li.cil.tis3d.common.tileentity.TileEntityController;
import net.minecraft.tileentity.TileEntity;

public final class MessageHandlerHaltAndCatchFire extends AbstractMessageHandlerWithLocation<MessageHaltAndCatchFire> {
    @Override
    protected void onMessageSynchronized(final MessageHaltAndCatchFire message, final MessageContext context) {
        final TileEntity tileEntity = getTileEntity(message, context);
        if (!(tileEntity instanceof TileEntityController)) {
            return;
        }

        final TileEntityController controller = (TileEntityController) tileEntity;
        controller.haltAndCatchFire();
    }
}
