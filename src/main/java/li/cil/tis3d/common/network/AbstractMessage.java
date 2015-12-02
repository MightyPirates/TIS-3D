package li.cil.tis3d.common.network;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

abstract class AbstractMessage implements IMessage {
    protected World getWorld(final MessageContext context, final int dimension) {
        switch (context.side) {
            case CLIENT:
                return getWorldClient(dimension);
            case SERVER:
                return getWorldServer(dimension);
        }
        return null;
    }

    public World getWorldClient(final int dimension) {
        final World world = FMLClientHandler.instance().getClient().theWorld;
        if (world == null) {
            return null;
        }
        if (world.provider.getDimensionId() != dimension) {
            return null;
        }
        return world;
    }

    public World getWorldServer(final int dimension) {
        return DimensionManager.getWorld(dimension);
    }
}
