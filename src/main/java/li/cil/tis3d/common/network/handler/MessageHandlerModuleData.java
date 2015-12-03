package li.cil.tis3d.common.network.handler;

import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.network.message.MessageModuleData;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageHandlerModuleData extends AbstractMessageHandlerWithLocation<MessageModuleData> {
    @Override
    protected void process(final MessageModuleData message, final MessageContext context) {
        final TileEntity tileEntity = getTileEntity(message, context);
        if (!(tileEntity instanceof TileEntityCasing)) {
            return;
        }

        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        final Module module = casing.getModule(message.getFace());
        if (module == null) {
            return;
        }

        module.onData(message.getNbt());
    }
}
