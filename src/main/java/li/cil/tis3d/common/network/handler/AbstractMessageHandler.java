package li.cil.tis3d.common.network.handler;

import li.cil.tis3d.charset.NetworkContext;
import li.cil.tis3d.charset.Packet;

import javax.annotation.Nullable;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public abstract class AbstractMessageHandler<T extends Packet> {
    @Nullable
    public void onMessage(final T message, final NetworkContext context) {
        onMessageSynchronized(message, context);
    }

    // --------------------------------------------------------------------- //

    protected abstract void onMessageSynchronized(final T message, final NetworkContext context);

    // --------------------------------------------------------------------- //

    @Nullable
    protected World getWorld(final DimensionType dimension, final NetworkContext context) {
        return context.getWorld(dimension).orElse(null);
    }
}
