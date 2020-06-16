package li.cil.tis3d.common.network.handler;

import li.cil.tis3d.common.network.message.AbstractMessageWithDimension;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class AbstractMessageHandlerWithDimension<T extends AbstractMessageWithDimension> extends AbstractMessageHandler<T> {
    @Nullable
    protected World getWorld(final T message, final PacketContext context) {
        return context.getPlayer().world;
    }
}
