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
        return context.getPlayer().world;
    }
}
