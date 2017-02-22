package li.cil.tis3d.common.network.handler;

import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public abstract class AbstractMessageHandler<T extends IMessage> implements IMessageHandler<T, IMessage> {
    @Override
    @Nullable
    public IMessage onMessage(final T message, final MessageContext context) {
        final IThreadListener thread = FMLCommonHandler.instance().getWorldThread(context.netHandler);
        if (thread.isCallingFromMinecraftThread()) {
            onMessageSynchronized(message, context);
        } else {
            thread.addScheduledTask(() -> onMessageSynchronized(message, context));
        }
        return null;
    }

    // --------------------------------------------------------------------- //

    protected void onMessageSynchronized(final T message, final MessageContext context) {
    }

    // --------------------------------------------------------------------- //

    @Nullable
    protected World getWorld(final int dimension, final MessageContext context) {
        switch (context.side) {
            case CLIENT:
                return getWorldClient(dimension);
            case SERVER:
                return getWorldServer(dimension);
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    private static World getWorldClient(final int dimension) {
        final World world = FMLClientHandler.instance().getClient().world;
        if (world == null) {
            return null;
        }
        if (world.provider.getDimension() != dimension) {
            return null;
        }
        return world;
    }

    @Nullable
    private static World getWorldServer(final int dimension) {
        return DimensionManager.getWorld(dimension);
    }
}
