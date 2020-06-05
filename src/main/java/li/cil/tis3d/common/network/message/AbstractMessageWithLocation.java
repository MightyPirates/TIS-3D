package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractMessageWithLocation extends AbstractMessageWithDimension {
    private BlockPos position;

    AbstractMessageWithLocation(final World world, final BlockPos position) {
        super(world);
        this.position = position;
    }

    AbstractMessageWithLocation() {
    }

    // --------------------------------------------------------------------- //

    public BlockPos getPosition() {
        return position;
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        super.fromBytes(buf);
        final PacketByteBuf buffer = new PacketByteBuf(buf);
        position = buffer.readBlockPos();

    }

    @Override
    public void toBytes(final ByteBuf buf) {
        super.toBytes(buf);
        final PacketByteBuf buffer = new PacketByteBuf(buf);
        buffer.writeBlockPos(position);
    }
}
