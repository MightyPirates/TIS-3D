package li.cil.tis3d.common.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

public abstract class AbstractMessageWithDimension implements IMessage {
    private int dimension;

    AbstractMessageWithDimension(final World world) {
        this.dimension = world.provider.dimensionId;
    }

    AbstractMessageWithDimension() {
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
