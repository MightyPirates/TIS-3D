package li.cil.tis3d.common.network.handler;

import li.cil.tis3d.common.network.message.AbstractMessageWithLocation;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import li.cil.tis3d.charset.NetworkContext;

import javax.annotation.Nullable;

public abstract class AbstractMessageHandlerWithLocation<T extends AbstractMessageWithLocation> extends AbstractMessageHandlerWithDimension<T> {
    @Nullable
    protected BlockEntity getTileEntity(final T message, final NetworkContext context) {
        final World world = getWorld(message, context);
        if (world == null) {
            return null;
        }
        // TODO: NPEs
        /* if (!world.isBlockLoaded(message.getPosition())) {
            return null;
        } */
        return world.getBlockEntity(message.getPosition());
    }
}
