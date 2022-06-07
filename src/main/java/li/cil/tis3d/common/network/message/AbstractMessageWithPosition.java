package li.cil.tis3d.common.network.message;

import li.cil.tis3d.util.LevelUtils;
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
    protected <T extends BlockEntity> void withBlockEntity(final Level level, final Class<T> type, final Consumer<T> callback) {
        if (LevelUtils.isLoaded(level, position)) {
            final BlockEntity blockEntity = level.getBlockEntity(position);
            if (blockEntity != null && type.isAssignableFrom(blockEntity.getClass())) {
                callback.accept((T) blockEntity);
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
