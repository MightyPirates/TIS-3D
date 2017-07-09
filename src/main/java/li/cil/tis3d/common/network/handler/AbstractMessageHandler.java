package li.cil.tis3d.common.network.handler;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public abstract class AbstractMessageHandler<T extends IMessage> implements IMessageHandler<T, IMessage> {
    @Override
    public IMessage onMessage(final T message, final MessageContext context) {
        onMessageSynchronized(message, context);
        return null;
    }

    // --------------------------------------------------------------------- //

    protected abstract void onMessageSynchronized(final T message, final MessageContext context);

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

    @SideOnly(Side.CLIENT)
    private static World getWorldClient(final int dimension) {
        final World world = FMLClientHandler.instance().getClient().theWorld;
        if (world == null) {
            return null;
        }
        if (world.provider.dimensionId != dimension) {
            return null;
        }
        return world;
    }

    private static World getWorldServer(final int dimension) {
        return DimensionManager.getWorld(dimension);
    }
}
