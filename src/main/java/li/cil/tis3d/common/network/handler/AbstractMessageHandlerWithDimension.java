package li.cil.tis3d.common.network.handler;

import li.cil.tis3d.common.network.message.AbstractMessageWithDimension;
import net.minecraft.world.World;
import li.cil.tis3d.charset.NetworkContext;

import javax.annotation.Nullable;

public abstract class AbstractMessageHandlerWithDimension<T extends AbstractMessageWithDimension> extends AbstractMessageHandler<T> {
    @Nullable
    protected World getWorld(final T message, final NetworkContext context) {
        return context.getWorld(message.getDimension()).orElse(null);
    }
}
