package li.cil.tis3d.common.network.message;

import li.cil.tis3d.util.WorldUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Consumer;

public abstract class AbstractMessageWithPosition extends AbstractMessage {
    protected BlockPos position;

    AbstractMessageWithPosition(final BlockPos position) {
        this.position = position;
    }

    AbstractMessageWithPosition(final PacketBuffer buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //

    @SuppressWarnings("unchecked")
    protected <T extends TileEntity> void withTileEntity(final World world, final Class<T> type, final Consumer<T> callback) {
        if (WorldUtils.isBlockLoaded(world, position)) {
            final TileEntity tileEntity = world.getTileEntity(position);
            if (tileEntity != null && type.isAssignableFrom(tileEntity.getClass())) {
                callback.accept((T) tileEntity);
            }
        }
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void fromBytes(final PacketBuffer buffer) {
        position = buffer.readBlockPos();
    }

    @Override
    public void toBytes(final PacketBuffer buffer) {
        buffer.writeBlockPos(position);
    }
}
