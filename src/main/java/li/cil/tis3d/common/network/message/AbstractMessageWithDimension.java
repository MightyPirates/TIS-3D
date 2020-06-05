package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public abstract class AbstractMessageWithDimension extends AbstractMessage {
    private DimensionType dimension;

    AbstractMessageWithDimension(final World world) {
        this.dimension = world.dimension.getType();
    }

    AbstractMessageWithDimension() {
    }

    // --------------------------------------------------------------------- //

    public DimensionType getDimension() {
        return dimension;
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        final PacketByteBuf buffer = new PacketByteBuf(buf);
        dimension = DimensionType.byRawId(buffer.readVarInt());
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        final PacketByteBuf buffer = new PacketByteBuf(buf);
        buffer.writeVarInt(dimension.getRawId());
    }
}
