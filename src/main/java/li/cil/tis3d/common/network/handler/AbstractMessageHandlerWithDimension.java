package li.cil.tis3d.common.network.handler;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import li.cil.tis3d.common.network.message.AbstractMessageWithDimension;
import net.minecraft.world.World;

public abstract class AbstractMessageHandlerWithDimension<T extends AbstractMessageWithDimension> extends AbstractMessageHandler<T> {
    protected World getWorld(final T message, final MessageContext context) {
        return getWorld(message.getDimension(), context);
    }
}
