package li.cil.tis3d.common.network.handler;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import li.cil.tis3d.common.network.message.AbstractMessageWithLocation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class AbstractMessageHandlerWithLocation<T extends AbstractMessageWithLocation> extends AbstractMessageHandlerWithDimension<T> {
    protected TileEntity getTileEntity(final T message, final MessageContext context) {
        final World world = getWorld(message, context);
        if (world == null) {
            return null;
        }
        if (!world.blockExists(message.getPositionX(), message.getPositionY(), message.getPositionZ())) {
            return null;
        }
        return world.getTileEntity(message.getPositionX(), message.getPositionY(), message.getPositionZ());
    }
}
