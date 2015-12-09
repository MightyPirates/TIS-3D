package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class AbstractMessageWithDimension implements IMessage {
    private int dimension;

    protected AbstractMessageWithDimension(final World world) {
        this.dimension = world.provider.getDimensionId();
    }

    protected AbstractMessageWithDimension() {
    }

    // --------------------------------------------------------------------- //

    public int getDimension() {
        return dimension;
    }

    // --------------------------------------------------------------------- //
    // IMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        final PacketBuffer buffer = new PacketBuffer(buf);
        dimension = buffer.readVarIntFromBuffer();

    }

    @Override
    public void toBytes(final ByteBuf buf) {
        final PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeVarIntToBuffer(dimension);
    }
}
