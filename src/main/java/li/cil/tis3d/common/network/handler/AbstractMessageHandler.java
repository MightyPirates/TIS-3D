package li.cil.tis3d.common.network.handler;

import li.cil.tis3d.common.network.message.AbstractMessage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import javax.annotation.Nullable;

public abstract class AbstractMessageHandler<T extends AbstractMessage> {
    public void onMessage(final T message, final PacketContext context) {
        if (context.getTaskQueue().isOnThread()) {
            onMessageSynchronized(message, context);
        } else {
            context.getTaskQueue().execute(() -> onMessageSynchronized(message, context));
        }
    }

    // --------------------------------------------------------------------- //

    protected abstract void onMessageSynchronized(final T message, final PacketContext context);

    // --------------------------------------------------------------------- //

    @Nullable
    protected World getWorld(final DimensionType dimension, final PacketContext context) {
        switch (context.getPacketEnvironment()) {
            case CLIENT:
                //noinspection MethodCallSideOnly Guarded by CLIENT switch case.
                return getWorldClient(dimension, context);
            case SERVER:
                return getWorldServer(dimension, context);
            default:
                return null;
        }
    }

    @Environment(EnvType.CLIENT)
    @Nullable
    private static World getWorldClient(final DimensionType dimension, final PacketContext context) {
        final PlayerEntity player = context.getPlayer();
        if (player == null) {
            return null;
        }

        final World world = player.world;
        if (world == null) {
            return null;
        }

        //~ if (world.getDimension().getType() != dimension) {
            //~ return null;
        //~ }

        return world;
    }

    @Nullable
    private static World getWorldServer(final DimensionType dimension, final PacketContext context) {
        final MinecraftServer server = context.getPlayer().getServer();
        if (server == null) {
            return null;
        }

        //~ return server.getWorld(dimension);
        return null; // XXX
    }
}
