package li.cil.tis3d.common.network.handler;

import li.cil.tis3d.common.network.message.AbstractMessageWithLocation;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class AbstractMessageHandlerWithLocation<T extends AbstractMessageWithLocation> extends AbstractMessageHandlerWithDimension<T> {
    @Nullable
    protected BlockEntity getBlockEntity(final T message, final PacketContext context) {
        final World world = getWorld(message, context);
        if (world == null) {
            return null;
        }
        //~ if (!world.isBlockLoaded(message.getPosition())) {
            //~ return null;
        //~ }
        return world.getBlockEntity(message.getPosition());
    }
}
