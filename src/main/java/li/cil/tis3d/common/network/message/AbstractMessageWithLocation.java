package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class AbstractMessageWithLocation implements IMessage {
    private int dimension;
    private BlockPos position;

    protected AbstractMessageWithLocation(final World world, final BlockPos position) {
        this.dimension = world.provider.getDimensionId();
        this.position = position;
    }

    protected AbstractMessageWithLocation() {
    }

    // --------------------------------------------------------------------- //

    public int getDimension() {
        return dimension;
    }

    public BlockPos getPosition() {
        return position;
    }

    // --------------------------------------------------------------------- //
    // IMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        final PacketBuffer buffer = new PacketBuffer(buf);
        dimension = buffer.readInt();
        position = buffer.readBlockPos();

    }

    @Override
    public void toBytes(final ByteBuf buf) {
        final PacketBuffer buffer = new PacketBuffer(buf);
        buf.writeInt(dimension);
        buffer.writeBlockPos(position);
    }
}
