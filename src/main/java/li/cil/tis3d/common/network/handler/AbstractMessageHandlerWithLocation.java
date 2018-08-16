package li.cil.tis3d.common.network.handler;

import li.cil.tis3d.common.network.message.AbstractMessageWithLocation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import pl.asie.protocharset.rift.network.NetworkContext;

import javax.annotation.Nullable;

public abstract class AbstractMessageHandlerWithLocation<T extends AbstractMessageWithLocation> extends AbstractMessageHandlerWithDimension<T> {
    @Nullable
    protected TileEntity getTileEntity(final T message, final NetworkContext context) {
        final World world = getWorld(message, context);
        if (world == null) {
            return null;
        }
        // TODO: NPEs
        /* if (!world.isBlockLoaded(message.getPosition())) {
            return null;
        } */
        return world.getTileEntity(message.getPosition());
    }
}
