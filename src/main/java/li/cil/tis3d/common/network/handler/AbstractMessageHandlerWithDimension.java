package li.cil.tis3d.common.network.handler;

import li.cil.tis3d.common.network.message.AbstractMessageWithDimension;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public abstract class AbstractMessageHandlerWithDimension<T extends AbstractMessageWithDimension> extends AbstractMessageHandler<T> {
    @Nullable
    protected World getWorld(final T message, final MessageContext context) {
        return getWorld(message.getDimension(), context);
    }
}
