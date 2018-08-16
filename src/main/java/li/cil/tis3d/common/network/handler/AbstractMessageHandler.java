package li.cil.tis3d.common.network.handler;

import net.minecraft.world.World;
import pl.asie.protocharset.rift.network.NetworkContext;
import pl.asie.protocharset.rift.network.Packet;

import javax.annotation.Nullable;

public abstract class AbstractMessageHandler<T extends Packet> {
    @Nullable
    public void onMessage(final T message, final NetworkContext context) {
        onMessageSynchronized(message, context);
    }

    // --------------------------------------------------------------------- //

    protected abstract void onMessageSynchronized(final T message, final NetworkContext context);

    // --------------------------------------------------------------------- //

    @Nullable
    protected World getWorld(final int dimension, final NetworkContext context) {
        return context.getWorld(dimension).orElse(null);
    }
}
