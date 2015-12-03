package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.common.network.handler.AbstractMessageHandlerWithLocation;
import li.cil.tis3d.common.network.message.MessageCasingState;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageHandlerCasingState extends AbstractMessageHandlerWithLocation<MessageCasingState> {
    @Override
    protected void process(final MessageCasingState message, final MessageContext context) {
        final TileEntity tileEntity = getTileEntity(message, context);
        if (!(tileEntity instanceof TileEntityCasing)) {
            return;
        }

        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        casing.setEnabled(message.isEnabled());
    }
}
