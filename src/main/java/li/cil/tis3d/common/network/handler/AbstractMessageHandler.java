package li.cil.tis3d.common.network.handler;

import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public abstract class AbstractMessageHandler<T extends IMessage> implements IMessageHandler<T, IMessage> {
    @Override
    public IMessage onMessage(final T message, final MessageContext context) {
        final IThreadListener thread = FMLCommonHandler.instance().getWorldThread(context.netHandler);
        if (thread.isCallingFromMinecraftThread()) {
            process(message, context);
        } else {
            thread.addScheduledTask(() -> process(message, context));
        }
        return null;
    }

    // --------------------------------------------------------------------- //

    protected abstract void process(final T message, final MessageContext context);

    // --------------------------------------------------------------------- //

    protected World getWorld(final int dimension, final MessageContext context) {
        switch (context.side) {
            case CLIENT:
                return getWorldClient(dimension);
            case SERVER:
                return getWorldServer(dimension);
        }
        return null;
    }

    protected World getWorldClient(final int dimension) {
        final World world = FMLClientHandler.instance().getClient().theWorld;
        if (world == null) {
            return null;
        }
        if (world.provider.getDimensionId() != dimension) {
            return null;
        }
        return world;
    }

    protected World getWorldServer(final int dimension) {
        return DimensionManager.getWorld(dimension);
    }
}
