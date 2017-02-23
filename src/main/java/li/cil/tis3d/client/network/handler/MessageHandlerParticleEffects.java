package li.cil.tis3d.client.network.handler;

import li.cil.tis3d.common.network.handler.AbstractMessageHandler;
import li.cil.tis3d.common.network.message.MessageParticleEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageHandlerParticleEffects extends AbstractMessageHandler<MessageParticleEffect> {
    @Override
    protected void onMessageSynchronized(final MessageParticleEffect message, final MessageContext context) {
        final World world = getWorld(message.getDimension(), context);
        if (world != null) {
            world.spawnParticle(message.getParticleType(), message.getX(), message.getY(), message.getZ(), 0, 0, 0);
        }
    }
}
