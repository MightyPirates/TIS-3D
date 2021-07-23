package li.cil.tis3d.common.network.message;

import li.cil.tis3d.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Consumer;

public abstract class AbstractMessageWithPosition extends AbstractMessage {
    protected BlockPos position;

    AbstractMessageWithPosition(final BlockPos position) {
        this.position = position;
    }

    AbstractMessageWithPosition(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //

    @SuppressWarnings("unchecked")
    protected <T extends BlockEntity> void withTileEntity(final Level world, final Class<T> type, final Consumer<T> callback) {
        if (WorldUtils.isLoaded(world, position)) {
            final BlockEntity tileEntity = world.getBlockEntity(position);
            if (tileEntity != null && type.isAssignableFrom(tileEntity.getClass())) {
                callback.accept((T) tileEntity);
            }
        }
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void fromBytes(final FriendlyByteBuf buffer) {
        position = buffer.readBlockPos();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buffer) {
        buffer.writeBlockPos(position);
    }
}
